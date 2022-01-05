package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.ArrayList;
import java.util.List;

public class ClassNode extends AbstractAnnotated {
    private TypeParameterized type;

    private List<FieldNode> fields = new ArrayList<>();
    private List<MethodNode> methods = new ArrayList<>();

    public ClassNode(TypeParameterized type) {
        this.type = type;
    }

    public TypeParameterized type() {
        return this.type;
    }

    public List<FieldNode> fields() {
        return fields;
    }

    public List<MethodNode> methods() {
        return methods;
    }

    public ClassNode addField(FieldNode field) {
        this.fields.add(field);
        return this;
    }

    public ClassNode addMethod(MethodNode method) {
        this.methods.add(method);
        return this;
    }

}
