package honeyroasted.cello.node.structure;

import honeyroasted.cello.environment.bytecode.signature.TypeVarScope;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassNode extends AbstractAnnotated {
    private TypeParameterized type;

    private ClassNode superclass;
    private List<ClassNode> interfaces = new ArrayList<>();

    private List<FieldNode> fields = new ArrayList<>();
    private List<MethodNode> methods = new ArrayList<>();

    private ClassNode outerClass;
    private List<ClassNode> innerClasses = new ArrayList<>();

    private TypeVarScope typeVarScope;

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

    public Optional<ClassNode> superclass() {
        return Optional.ofNullable(superclass);
    }

    public Optional<ClassNode> outerclass() {
        return Optional.ofNullable(this.outerClass);
    }

    public List<ClassNode> innerClasses() {
        return innerClasses;
    }

    public TypeVarScope typeVarScope() {
        return typeVarScope;
    }

    public ClassNode setTypeVarScope(TypeVarScope typeVarScope) {
        this.typeVarScope = typeVarScope;
        return this;
    }

    public ClassNode setOuterClass(ClassNode outerClass) {
        this.outerClass = outerClass;
        return this;
    }

    public ClassNode addInnerClass(ClassNode innerClass) {
        this.innerClasses.add(innerClass);
        return this;
    }

    public ClassNode setSuperclass(ClassNode superclass) {
        this.superclass = superclass;
        return this;
    }

    public ClassNode addInterface(ClassNode inter) {
        this.interfaces.add(inter);
        return this;
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
