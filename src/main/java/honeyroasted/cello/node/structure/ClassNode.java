package honeyroasted.cello.node.structure;

import honeyroasted.cello.environment.TypeVarScope;
import honeyroasted.cello.node.modifier.Access;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassNode extends AbstractAnnotated {
    private TypeParameterized type;

    private ClassNode superclass;
    private List<ClassNode> interfaces = new ArrayList<>();

    private List<FieldNode> fields = new ArrayList<>();
    private List<MethodNode> methods = new ArrayList<>();

    private ClassNode outerClass;
    private MethodNode outerMethod;
    private ClassNode nestHost;
    private List<InnerClassNode> innerClasses = new ArrayList<>();
    private List<ClassNode> nestClasses = new ArrayList<>();
    private List<Namespace> permittedSubclasses = new ArrayList<>();

    private TypeVarScope typeVarScope = new TypeVarScope();

    public ClassNode(TypeParameterized type) {
        this.type = type;
    }

    public List<MethodNode> lookupMethods(Predicate<MethodNode> predicate) {
        List<MethodNode> nodes = this.methods.stream().filter(predicate).collect(Collectors.toList());

        if (this.superclass != null) {
            nodes.addAll(this.superclass.lookupMethods(predicate));
        }

        for (ClassNode inter : this.interfaces) {
            nodes.addAll(inter.lookupMethods(predicate));
        }

        return nodes;
    }

    public Optional<MethodNode> outerMethod() {
        return Optional.ofNullable(this.outerMethod);
    }

    public ClassNode setOuterMethod(MethodNode outerMethod) {
        this.outerMethod = outerMethod;
        return this;
    }

    public ClassNode nestHost() {
        return nestHost;
    }

    public ClassNode setNestHost(ClassNode nestHost) {
        this.nestHost = nestHost;
        return this;
    }

    public List<FieldNode> lookupFields(Predicate<FieldNode> predicate) {
        List<FieldNode> nodes = this.fields.stream().filter(predicate).collect(Collectors.toList());

        if (this.superclass != null) {
            nodes.addAll(this.superclass.lookupFields(predicate));
        }

        for (ClassNode inter : this.interfaces) {
            nodes.addAll(inter.lookupFields(predicate));
        }

        return nodes;
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

    public List<Namespace> permittedSubclasses() {
        return this.permittedSubclasses;
    }

    public ClassNode addPermittedSubclass(Namespace namespace) {
        this.permittedSubclasses.add(namespace);
        return this;
    }

    public List<InnerClassNode> innerClasses() {
        return innerClasses;
    }

    public TypeVarScope typeVarScope() {
        return typeVarScope;
    }

    public ClassNode setTypeVarScope(TypeVarScope typeVarScope) {
        this.typeVarScope = typeVarScope;
        return this;
    }

    public List<ClassNode> interfaces() {
        return this.interfaces;
    }

    public ClassNode outerClass() {
        return this.outerClass;
    }

    public List<ClassNode> nestClasses() {
        return this.nestClasses;
    }

    public ClassNode addNestClass(ClassNode nest) {
        this.nestClasses.add(nest);
        return this;
    }

    public ClassNode setOuterClass(ClassNode outerClass) {
        this.outerClass = outerClass;
        return this;
    }

    public ClassNode addInnerClass(InnerClassNode innerClass) {
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
