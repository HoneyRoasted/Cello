package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassNode extends AbstractParameterized {
    private TypeParameterized type;

    private ClassNode arrayElement;

    private ClassNode superclass;
    private List<ClassNode> interfaces = new ArrayList<>();

    private ClassNode outerClass;
    private MethodNode outerMethod;
    private List<InnerClassNode> innerClasses = new ArrayList<>();

    private ClassNode nestHost;
    private List<ClassNode> nestClasses = new ArrayList<>();

    private List<Namespace> permittedSubclasses = new ArrayList<>();

    private List<FieldNode> fields = new ArrayList<>();
    private List<MethodNode> methods = new ArrayList<>();

    public ClassNode(TypeParameterized type) {
        this.type = type;
        this.parent = () -> {
            if (this.outerMethod != null) {
                return this.outerMethod;
            }

            if (this.outerClass != null) {
                Optional<InnerClassNode> self = this.outerClass.innerClasses().stream().filter(i -> i.classNode() == this).findFirst();
                if (self.isPresent() && !self.get().modifiers().has(Modifier.STATIC)) {
                    return self.get().classNode();
                }
            }

            return null;
        };
    }

    public TypeClass type(TypeInformal... arguments) {
        if (this.type.namespace().isArray() && this.arrayElement != null) {
            return (TypeClass) this.arrayElement.type(arguments).array(1);
        } else {
            return this.type.withArguments(arguments);
        }
    }

    public TypeParameterized parameterizedType() {
        return this.type;
    }

    public ClassNode setParameterizedType(TypeParameterized type) {
        this.type = type;
        return this;
    }

    public ClassNode arrayElement() {
        return this.arrayElement;
    }

    public ClassNode setArrayElement(ClassNode arrayElement) {
        this.arrayElement = arrayElement;
        return this;
    }

    public ClassNode superclass() {
        return this.superclass;
    }

    public ClassNode setSuperclass(ClassNode superclass) {
        this.superclass = superclass;
        return this;
    }

    public List<ClassNode> interfaces() {
        return this.interfaces;
    }

    public ClassNode setInterfaces(List<ClassNode> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public ClassNode addInterface(ClassNode inter) {
        this.interfaces.add(inter);
        return this;
    }

    public ClassNode outerClass() {
        return this.outerClass;
    }

    public ClassNode setOuterClass(ClassNode outerClass) {
        this.outerClass = outerClass;
        return this;
    }

    public MethodNode outerMethod() {
        return this.outerMethod;
    }

    public ClassNode setOuterMethod(MethodNode outerMethod) {
        this.outerMethod = outerMethod;
        return this;
    }

    public List<InnerClassNode> innerClasses() {
        return this.innerClasses;
    }

    public ClassNode setInnerClasses(List<InnerClassNode> innerClasses) {
        this.innerClasses = innerClasses;
        return this;
    }

    public ClassNode addInnerClass(InnerClassNode node) {
        this.innerClasses.add(node);
        return this;
    }

    public ClassNode nestHost() {
        return this.nestHost;
    }

    public ClassNode setNestHost(ClassNode nestHost) {
        this.nestHost = nestHost;
        return this;
    }

    public List<ClassNode> nestClasses() {
        return this.nestClasses;
    }

    public ClassNode setNestClasses(List<ClassNode> nestClasses) {
        this.nestClasses = nestClasses;
        return this;
    }

    public ClassNode addNestClass(ClassNode nestClass) {
        this.nestClasses.add(nestClass);
        return this;
    }

    public List<Namespace> permittedSubclasses() {
        return this.permittedSubclasses;
    }

    public ClassNode setPermittedSubclasses(List<Namespace> permittedSubclasses) {
        this.permittedSubclasses = permittedSubclasses;
        return this;
    }

    public ClassNode addPermittedSubclass(Namespace namespace) {
        this.permittedSubclasses.add(namespace);
        return this;
    }

    public List<FieldNode> fields() {
        return this.fields;
    }

    public ClassNode setFields(List<FieldNode> fields) {
        this.fields = fields;
        return this;
    }

    public ClassNode addField(FieldNode field) {
        this.fields.add(field);
        return this;
    }

    public List<MethodNode> methods() {
        return this.methods;
    }

    public ClassNode setMethods(List<MethodNode> methods) {
        this.methods = methods;
        return this;
    }

    public ClassNode addMethod(MethodNode method) {
        this.methods.add(method);
        return this;
    }
}
