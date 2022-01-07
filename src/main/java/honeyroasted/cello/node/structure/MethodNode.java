package honeyroasted.cello.node.structure;

import honeyroasted.cello.environment.TypeVarScope;
import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.modifier.Modifiable;
import honeyroasted.cello.node.modifier.Modifiers;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.cello.properties.Properties;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.method.TypeMethodParameterized;
import honeyroasted.javatype.parameterized.TypeVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodNode extends AbstractAnnotated implements Node {
    private TypeInformal ret = Types.VOID;

    private List<ParameterNode> parameters = new ArrayList<>();
    private List<TypeVar> typeParameters = new ArrayList<>();

    private List<TypeInformal> exceptions = new ArrayList<>();

    private String name;
    private ClassNode owner;

    private TypeMethodParameterized erased;

    private TypeVarScope typeVarScope = new TypeVarScope();

    public MethodNode(String name, ClassNode owner) {
        this.name = name;
        this.owner = owner;
    }

    public TypeVarScope typeVarScope() {
        return typeVarScope;
    }

    public MethodNode setTypeVarScope(TypeVarScope scope) {
        this.typeVarScope = scope;
        return this;
    }

    public ClassNode owner() {
        return this.owner;
    }

    public TypeMethodParameterized erased() {
        return erased;
    }

    public MethodNode setErased(TypeMethodParameterized erased) {
        this.erased = erased;
        return this;
    }

    public TypeMethodParameterized type() {
        return Types.method()
                .returnType(this.ret)
                .parameters(this.parameters.stream().map(ParameterNode::type).collect(Collectors.toList()))
                .typeParameters(this.typeParameters)
                .exceptions(this.exceptions)
                .build();
    }

    public MethodNode setType(TypeMethodParameterized type) {
        this.setReturn(type.returnType());

        this.typeParameters = new ArrayList<>(type.typeParameters());
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

    public List<TypeInformal> exceptions() {
        return exceptions;
    }

    public MethodNode addException(TypeInformal type) {
        this.exceptions.add(type);
        return this;
    }

    public MethodNode addTypeParameter(String name, TypeInformal... bounds) {
        return this.addTypeParameter(name, Arrays.asList(bounds));
    }

    public MethodNode addTypeParameter(String name, List<TypeInformal> bounds) {
        this.typeParameters.add(new TypeVar(name, bounds));
        return this;
    }

    public MethodNode addParameter(TypeInformal type, String name) {
        this.parameters.add(new ParameterNode(type, name));
        return this;
    }

    public MethodNode setReturn(TypeInformal ret) {
        this.ret = ret;
        return this;
    }

    public TypeInformal ret() {
        return ret;
    }

    public List<ParameterNode> parameters() {
        return parameters;
    }

    public List<TypeVar> typeParameters() {
        return typeParameters;
    }

    public String name() {
        return name;
    }

}
