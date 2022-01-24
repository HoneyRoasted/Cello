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
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InvokeVirtual extends AbstractNode implements Node {
    @Child
    private Node source;
    private String name;
    @Child
    private List<Node> parameters;

    public InvokeVirtual(Node source, String name, List<Node> parameters) {
        this.source = source;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        this.source.type();


        return null;
    }

    public static Verification<MethodNode> lookupVirtualMethod(Node owner, Node source, String name, List<Node> parameters, Environment environment, CodeContext context) {
        VerificationBuilder<MethodNode> builder = Verification.builder();
        builder.source(owner);

        Set<TypeClass> types = TypeUtil.flatten(source.type());
        List<List<MethodNode>> methodCandidates = new ArrayList<>();

        for (TypeClass type : types) {
            Verification<ClassNode> lookup = environment.lookup(type);
            builder.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                methodCandidates.add(lookup.value().get().lookupMethods(m -> m.name().equals(name)));
            }
        }

        int max = methodCandidates.stream().mapToInt(List::size).max().orElse(0);

        List<MethodNode> methods = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            for (List<MethodNode> nodes : methodCandidates) {
                if (i < nodes.size() && !methods.contains(nodes.get(i))) {
                    methods.add(nodes.get(i));
                }
            }
        }

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found", types.stream().map(Type::externalName).toList(), name).build();
        }

        methods = methods.stream().filter(m -> m.parameters().size() == parameters.size() || (m.modifiers().has(Modifier.VARARGS) && m.parameters().size() <= parameters.size())).toList();

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found for argument count %s", types.stream().map(Type::externalName).toList(), name, parameters.size()).build();
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
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found for given argument types", types.stream().map(Type::externalName).toList(), name).build();
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
                    if (!isNarrower(a, b)) {
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

    public static boolean isNarrower(MethodNode a, MethodNode b) {
        if (a.parameters().size() == b.parameters().size()) {
            for (int i = 0; i < a.parameters().size(); i++) {
                ParameterNode pa = a.parameters().get(i);
                ParameterNode ba = b.parameters().get(i);

                if (!pa.type().isAssignableTo(ba.type())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {

    }

}
