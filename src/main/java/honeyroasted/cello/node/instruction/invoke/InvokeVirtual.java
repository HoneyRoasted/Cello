package honeyroasted.cello.node.instruction.invoke;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.node.structure.ParameterNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
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
    @Child(order = Child.BOTH)
    private List<Node> parameters;

    public InvokeVirtual(Node source, String name, List<Node> parameters) {
        this.source = source;
        this.name = name;
        this.parameters = parameters;
    }

    private FilledMethod target;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        Verification<FilledMethod> target = lookupMethod(this, this.source.type(), this.name, this.parameters, environment, context, false, false);
        if (target.success() && target.value().isPresent()) {
            this.target = target.value().get();
            List<TypeInformal> paramTypes = this.target.params();
            if (this.target.method().modifiers().has(Modifier.VARARGS) && !paramTypes.isEmpty()) {
                paramTypes.remove(paramTypes.size() - 1);
            }

            List<Node> convertParams = new ArrayList<>();
            for (int i = 0; i < paramTypes.size(); i++) {
                convertParams.add(Nodes.convert(this.parameters.get(i), paramTypes.get(i)));
            }

            if (this.target.method().modifiers().has(Modifier.VARARGS)) {
                TypeInformal vararg = this.target.params().get(this.target.params().size() - 1);
                if (vararg instanceof TypeArray arr) {
                    TypeInformal element = arr.element();
                    for (int i = paramTypes.size(); i < this.parameters.size(); i++) {
                        convertParams.add(Nodes.convert(this.parameters.get(i), element));
                    }
                }
            }

            this.parameters = convertParams;
        }
        return target.map(FilledMethod::ret);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        int size = this.target.params().size();
        if (this.target.method().modifiers().has(Modifier.VARARGS)) {
            size -= 1;
        }

        for (int i = 0; i < size; i++) {
            this.parameters.get(i).apply(adapter, environment, context);
        }

        if (this.target.method().modifiers().has(Modifier.VARARGS)) {
            int arrSize = this.parameters.size() - size;
            adapter.iconst(arrSize);
            TypeArray arr = (TypeArray) this.target.method().erased().parameters().get(size);
            adapter.newarray(TypeUtil.asmType(arr.element()));
            for (int i = 0; i < arrSize; i++) {
                adapter.dup();
                adapter.iconst(i);
                Node curr = this.parameters.get(i + size);
                curr.apply(adapter, environment, context);
                adapter.astore(TypeUtil.asmType(arr.element()));
            }
        }

        this.source.apply(adapter, environment, context);
        adapter.invokevirtual(this.target.method().owner().parameterizedType().internalName(), this.name,
                this.target.method().erased().descriptor(),
                this.target.method().owner().modifiers().has(Modifier.INTERFACE));
    }

    public static Verification<FilledMethod> lookupMethod(Node owner, Namespace sourceType, String name, List<Node> parameters, Environment environment, CodeContext context, boolean staticMethod, boolean specialMethod) {
        return lookupMethod(owner, Types.parameterized().namespace(sourceType).build().withArguments(), name, parameters, environment, context, staticMethod, specialMethod);
    }

    public static Verification<FilledMethod> lookupMethod(Node owner, TypeInformal sourceType, String name, List<Node> parameters, Environment environment, CodeContext context, boolean staticMethod, boolean specialMethod) {
        VerificationBuilder<FilledMethod> builder = Verification.builder();
        builder.source(owner);

        Set<TypeClass> types = TypeUtil.flatten(sourceType);
        List<List<MethodNode>> methodCandidates = new ArrayList<>();

        for (TypeClass type : types) {
            Verification<ClassNode> lookup = environment.lookup(type);
            builder.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                if (specialMethod) {
                    methodCandidates.add(lookup.value().get().methods().stream().filter(m -> m.name().equals(name)).toList());
                } else {
                    methodCandidates.add(lookup.value().get().lookupMethods(m -> m.name().equals(name)));
                }
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

        List<FilledMethod> filledMethods = new ArrayList<>();
        for (MethodNode method : methods) {
            Verification<FilledMethod> filledVerif = FilledMethod.attemptFill(method, sourceType, parameters.stream().map(Node::type).toList(), environment, context);
            builder.child(filledVerif);
            if (filledVerif.success() && filledVerif.value().isPresent()) {
                filledMethods.add(filledVerif.value().get());
            }
        }

        filledMethods = filledMethods.stream().filter(m -> {
            VerificationBuilder<MethodNode> methodVerify = Verification.builder();
            methodVerify.source(owner);

            MethodNode node = m.method();
            List<TypeInformal> methodParams = m.params();

            if (node.modifiers().has(Modifier.VARARGS) && !methodParams.isEmpty()) {
                methodParams.remove(methodParams.size() - 1);
            }

            boolean flag = true;
            for (int i = 0; i < parameters.size() && i < methodParams.size(); i++) {
                Node arg = parameters.get(i);
                TypeInformal param = methodParams.get(i);
                if (!arg.type().isAssignableTo(param)) {
                    methodVerify.child(Verification.error(owner, Verify.Code.TYPE_ERROR, "'%s' is not assignable to '%s' (parameter %s, argument #%s)", arg.type(), param, node.parameters().get(i).name(), i + 1));
                    flag = false;
                } else {
                    methodVerify.child(Verification.success(owner, arg));
                }
            }

            if (node.modifiers().has(Modifier.VARARGS) && !node.parameters().isEmpty()) {
                ParameterNode vararg = node.parameters().get(node.parameters().size() - 1);
                TypeInformal varargType = m.params().get(m.params().size() - 1);

                if (varargType instanceof TypeArray arr) {
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
                    methodVerify.child(Verification.error(owner, Verify.Code.TYPE_ERROR, "Vararg parameter '%s' in method '%s' is not an array", vararg.name(), node.externalName()));
                    flag = false;
                }
            }

            if (flag) {
                builder.child(methodVerify.message("Possible method candidate '%s'", node.externalName()).build());
            } else {
                builder.child(methodVerify.error(Verify.Code.CHILD_FAILED_ERROR, "Parameter mismatch for method candidate '%s'", node.externalName()).build());
            }

            return flag;
        }).toList();

        if (filledMethods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method '%s::%s' not found for given argument types", types.stream().map(Type::externalName).toList(), name).build();
        }

        List<FilledMethod> previous = new ArrayList<>(filledMethods);
        filledMethods = filledMethods.stream().filter(m -> context.owner().owner().accessTo(m.method().owner()).canAccess(m.method().modifiers().access())).toList();

        if (filledMethods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method(s) %s are not accessible from class '%s'",
                    previous.stream().map(m -> m.method().externalName()).toList(), context.owner().owner().externalName()).build();
        }

        previous = new ArrayList<>(filledMethods);
        if (staticMethod) {
            methods = methods.stream().filter(m -> m.modifiers().has(Modifier.STATIC)).toList();
        } else {
            methods = methods.stream().filter(m -> !m.modifiers().has(Modifier.STATIC)).toList();
        }

        if (methods.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "Method(s) %s are " + (staticMethod ? "not " : "") + " static",
                    previous.stream().map(m -> m.method().externalName()).toList()).build();
        }

        List<FilledMethod> res = new ArrayList<>();

        outer:
        for (int i = 0; i < filledMethods.size(); i++) {
            FilledMethod a = filledMethods.get(i);

            for (int j = 0; j < filledMethods.size(); j++) {
                if (i != j) {
                    FilledMethod b = filledMethods.get(j);
                    if (!isNarrower(a, b)) {
                        continue outer;
                    }
                }
            }

            res.add(a);
        }

        if (res.size() >= 2 && !res.get(0).method().owner().equals(res.get(1).method().owner())) {
            return builder.value(res.get(0)).build();
        }

        if (res.isEmpty()) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "'%s' is ambiguous, possible methods include %s", name,
                    methods.stream().map(MethodNode::externalName).toList()).build();
        } else if (res.size() > 1) {
            return builder.error(Verify.Code.METHOD_NOT_FOUND, "'%s' is ambiguous, possible methods include %s", name,
                    res.stream().map(m -> m.method().externalName()).toList()).build();
        }

        return builder.value(res.get(0)).build();
    }

    public static boolean isNarrower(FilledMethod a, FilledMethod b) {
        if (a.params().size() == b.params().size()) {
            for (int i = 0; i < a.params().size(); i++) {
                TypeInformal pa = a.params().get(i);
                TypeInformal ba = b.params().get(i);

                if (!pa.isAssignableTo(ba)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

}
