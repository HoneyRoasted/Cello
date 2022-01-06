package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
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

    public MethodNode(String name) {
        this.name = name;
    }

    public TypeMethodParameterized type() {
        return Types.method()
                .returnType(this.ret)
                .parameters(this.parameters.stream().map(ParameterNode::type).collect(Collectors.toList()))
                .typeParameters(this.typeParameters)
                .exceptions(this.exceptions)
                .build();
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
