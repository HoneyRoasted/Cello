package honeyroasted.cello.node.instruction.invoke;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.node.structure.ParameterNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InvokeSpecial extends AbstractNode implements Node {
    @Child
    private Node source;
    private TypeParameterized sourceType;
    private String name;
    @Child
    private List<Node> parameters;

    public InvokeSpecial(Node source, TypeParameterized sourceType, String name, List<Node> parameters) {
        this.source = source;
        this.sourceType = sourceType;
        this.name = name;
        this.parameters = parameters;
    }

    public static Verification<MethodNode> lookupSpecialMethod(Node owner, TypeParameterized type, String name, List<Node> parameters, Environment environment, CodeContext context) {
        VerificationBuilder<MethodNode> builder = Verification.builder();
        builder.source(owner);

        List<MethodNode> methods = new ArrayList<>();
        Verification<ClassNode> lookup = environment.lookup(type);
        builder.child(lookup);

        if (lookup.success() && lookup.value().isPresent()){
            methods.addAll(lookup.value().get().methods().stream().filter(m -> m.name().equals(name)).toList());
        }

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found", type.externalName(), name).build();
        }

        methods = methods.stream().filter(m -> m.parameters().size() == parameters.size() || (m.modifiers().has(Modifier.VARARGS) && m.parameters().size() <= parameters.size())).toList();

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found for argument count %s", type.externalName(), name, parameters.size()).build();
        }

        methods = methods.stream().filter(m -> {
            VerificationBuilder<MethodNode> methodVerify = Verification.builder();
            methodVerify.source(owner);

            List<ParameterNode> methodParams = new ArrayList<>(m.parameters());

            if (m.modifiers().has(Modifier.VARARGS) && !methodParams.isEmpty()) {
                methodParams.remove(methodParams.size() - 1);
            }

            boolean flag = true;
            for (int i = 0; i < parameters.size() && i < methodParams.size(); i++) {
                Node arg = parameters.get(i);
                ParameterNode param = methodParams.get(i);
                if (!arg.type().isAssignableTo(param.type())) {
                    methodVerify.child(Verification.error(owner, Verify.Code.TYPE_ERROR, "'%s' is not assignable to '%s' (parameter %s, argument #%s)", arg.type(), param.type(), param.name(), i + 1));
                    flag = false;
                } else {
                    methodVerify.child(Verification.success(owner, arg));
                }
            }

            if (m.modifiers().has(Modifier.VARARGS) && !m.parameters().isEmpty()) {
                ParameterNode vararg = m.parameters().get(m.parameters().size() - 1);

                if (vararg.type() instanceof TypeArray arr) {
                    TypeInformal element = arr.element();
                    for (int i = methodParams.size(); i < parameters.size(); i++) {
                        Node arg = parameters.get(i);

                        if (!arg.type().isAssignableTo(element)) {
                            methodVerify.child(Verification.error(owner, Verify.Code.TYPE_ERROR, "'%s' is not assignable to '%s' (parameter %s, argument #%s)", arg.type(), element, vararg.name(), i + 1));
                            flag = false;
                        } else {
                            methodVerify.child(Verification.success(owner, arg));
                        }
                    }
                } else {
                    methodVerify.child(Verification.error(owner, Verify.Code.TYPE_ERROR, "Vararg parameter '%s' in method '%s' is not an array", vararg.name(), m.externalName()));
                    flag = false;
                }
            }

            if (flag) {
                builder.child(methodVerify.message("Possible method candidate '%s'", m.externalName()).build());
            } else {
                builder.child(methodVerify.error(Verify.Code.CHILD_FAILED_ERROR, "Parameter mismatch for method candidate '%s'", m.externalName()).build());
            }

            return flag;
        }).toList();

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found for given argument types", type.externalName(), name).build();
        }

        List<MethodNode> previous = new ArrayList<>(methods);
        methods = methods.stream().filter(m -> context.owner().owner().accessTo(m.owner()).canAccess(m.modifiers().access())).toList();

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method(s) %s is/are not accessible from class '%s'",
                    previous.stream().map(MethodNode::externalName).toList(), context.owner().owner().externalName()).build();
        }

        previous = new ArrayList<>(methods);
        methods = methods.stream().filter(m -> !m.modifiers().has(Modifier.STATIC)).toList();

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method(s) %s is/are static",
                    previous.stream().map(MethodNode::externalName).toList()).build();
        }

        List<MethodNode> res = new ArrayList<>();

        outer:
        for (int i = 0; i < methods.size(); i++) {
            MethodNode a = methods.get(i);

            for (int j = 0; j < methods.size(); j++) {
                if (i != j) {
                    MethodNode b = methods.get(j);
                    if (!InvokeVirtual.isNarrower(a, b)) {
                        continue outer;
                    }
                }
            }

            res.add(a);
        }

        if (res.size() >= 2 && !res.get(0).owner().equals(res.get(1).owner())) {
            return builder.value(res.get(0)).build();
        }

        if (res.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "'%s' is ambiguous, possible methods include %s", name,
                    methods.stream().map(MethodNode::externalName).toList()).build();
        } else if (res.size() > 1) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "'%s' is ambiguous, possible methods include %s", name,
                    res.stream().map(MethodNode::externalName).toList()).build();
        }

        return builder.value(res.get(0)).build();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return null;
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {

    }

}
