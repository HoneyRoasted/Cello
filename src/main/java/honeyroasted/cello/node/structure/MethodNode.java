package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.modifier.Modifiers;
import honeyroasted.cello.properties.Properties;
import honeyroasted.cello.properties.PropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.method.TypeMethodParameterized;
import honeyroasted.javatype.parameterized.TypeVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodNode extends AbstractParameterized implements PropertyHolder {
    private TypeInformal returnType = Types.VOID;

    private List<ParameterNode> parameters = new ArrayList<>();
    private List<TypeInformal> exceptions = new ArrayList<>();
    private List<ClassNode> innerClasses = new ArrayList<>();

    private String name;
    private ClassNode owner;

    private TypeMethodParameterized erased;

    private Node body;

    public MethodNode(String name, ClassNode owner) {
        this.name = name;
        this.owner = owner;
        this.parent = () -> {
            if (!this.modifiers().has(Modifier.STATIC)) {
                return this.owner;
            } else {
                return null;
            }
        };
    }

    public Optional<Node> body() {
        return Optional.ofNullable(this.body);
    }

    public MethodNode setBody(Node body) {
        this.body = body;
        return this;
    }

    public String externalName() {
        StringBuilder builder = new StringBuilder();

        if (!this.definedTypeVars().isEmpty()) {
            builder.append("<");
            for (int i = 0; i < this.definedTypeVars().size(); i++) {
                TypeVar var = this.definedTypeVars().get(i);
                builder.append(var.externalName());
                if (i < this.definedTypeVars().size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append("> ");
        }

        builder.append(this.returnType.externalName()).append(" ").append(this.owner.externalName()).append("::").append(this.name).append(" ").append("(");

        for (int i = 0; i < parameters.size(); i++) {
            ParameterNode node = parameters.get(i);
            builder.append(node.type().externalName()).append(" ").append(node.name());
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.append(")").toString();
    }

    public TypeMethodParameterized type() {
        return Types.method()
                .returnType(this.returnType)
                .parameters(this.parameters.stream().map(ParameterNode::type).collect(Collectors.toList()))
                .typeParameters(this.definedTypeVars())
                .exceptions(this.exceptions)
                .build();
    }

    public MethodNode setType(TypeMethodParameterized type) {
        this.setReturnType(type.returnType());

        this.setTypeVars(type.typeParameters());
        this.exceptions = new ArrayList<>(type.exceptions());

        List<ParameterNode> parameters = new ArrayList<>();

        for (int i = 0; i < type.parameters().size(); i++) {
            String name = "arg" + i;
            Modifiers modifiers = new Modifiers();
            Properties properties = new Properties();

            if (i < this.parameters.size()) {
                ParameterNode node = this.parameters.get(i);
                name = node.name();
                modifiers = node.modifiers();
                properties = node.properties();
            }

            ParameterNode node = new ParameterNode(type.parameters().get(i), name);
            node.modifiers().set(modifiers);
            parameters.add(node.withProperties(properties));
        }

        this.parameters = parameters;
        return this;
    }

    public TypeInformal returnType() {
        return this.returnType;
    }

    public MethodNode setReturnType(TypeInformal returnType) {
        this.returnType = returnType;
        return this;
    }

    public List<ParameterNode> parameters() {
        return this.parameters;
    }

    public MethodNode setParameters(List<ParameterNode> parameters) {
        this.parameters = parameters;
        return this;
    }

    public MethodNode addParameter(ParameterNode node) {
        this.parameters.add(node);
        return this;
    }

    public List<TypeInformal> exceptions() {
        return this.exceptions;
    }

    public MethodNode setExceptions(List<TypeInformal> exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    public MethodNode addException(TypeInformal exception) {
        this.exceptions.add(exception);
        return this;
    }

    public List<ClassNode> innerClasses() {
        return this.innerClasses;
    }

    public MethodNode setInnerClasses(List<ClassNode> innerClasses) {
        this.innerClasses = innerClasses;
        return this;
    }

    public MethodNode addInnerClass(ClassNode innerClass) {
        this.innerClasses.add(innerClass);
        return this;
    }

    public String name() {
        return this.name;
    }

    public MethodNode setName(String name) {
        this.name = name;
        return this;
    }

    public ClassNode owner() {
        return this.owner;
    }

    public MethodNode setOwner(ClassNode owner) {
        this.owner = owner;
        return this;
    }

    public TypeMethodParameterized erased() {
        return this.erased;
    }

    public MethodNode setErased(TypeMethodParameterized erased) {
        this.erased = erased;
        return this;
    }

    @Override
    public String toString() {
        return externalName();
    }
}
