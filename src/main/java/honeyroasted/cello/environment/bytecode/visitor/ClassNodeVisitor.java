package honeyroasted.cello.environment.bytecode.visitor;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.visitor.signature.ClassSignatureVisitor;
import honeyroasted.cello.environment.bytecode.visitor.signature.MethodSignatureVisitor;
import honeyroasted.cello.environment.bytecode.visitor.signature.TypeSignatureVisitor;
import honeyroasted.cello.environment.bytecode.visitor.annotation.AnnotationNodeVisitor;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.modifier.ModifierTarget;
import honeyroasted.cello.node.modifier.Modifiers;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.node.structure.InnerClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.node.structure.ParameterNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

public class ClassNodeVisitor extends ClassVisitor {
    private ClassNode node;

    private Environment environment;
    private VerificationBuilder<ClassNode> verification = Verification.builder();

    private List<Runnable> delay = new ArrayList<>();

    public ClassNodeVisitor(ClassNode node, Environment environment) {
        super(ASM9);
        this.node = node;
        this.environment = environment;
        this.verification.value(this.node);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaceNames) {
        Namespace namespace = Namespace.internal(name);
        this.node.modifiers().set(Modifiers.fromBits(access, ModifierTarget.CLASS));

        TypeParameterized.Builder builder = TypeParameterized.builder().namespace(namespace);

        TypeParameterized superclass = null;
        List<TypeParameterized> interfaces = new ArrayList<>();

        if (superName != null) {
            Verification<ClassNode> lookup = this.environment.lookup(Namespace.internal(superName));
            this.verification.child(lookup);
            if (lookup.success() && lookup.value().isPresent()) {
                superclass = lookup.value().get().parameterizedType();
                builder.superclass(superclass.withArguments());
                this.node.setSuperclass(lookup.value().get());
            }
        }

        for (String interfaceName : interfaceNames) {
            Verification<ClassNode> lookup = this.environment.lookup(Namespace.internal(interfaceName));
            this.verification.child(lookup);
            if (lookup.success() && lookup.value().isPresent()) {
                ClassNode inter = lookup.value().get();
                interfaces.add(inter.parameterizedType());
                builder.addInterface(inter.parameterizedType().withArguments());
                this.node.addInterface(inter);
            }
        }

        if (signature != null) {
            ClassSignatureVisitor visitor = new ClassSignatureVisitor(v -> {
                this.verification.child(v);
                if (v.success() && v.value().isPresent()) {
                    builder.from(v.value().get());
                }
            }, namespace, this.node, superclass, interfaces, this.environment);

            SignatureReader reader = new SignatureReader(signature);
            reader.accept(visitor);
            visitor.visitFinish();
        }

        builder.build(this.node.parameterizedType());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Namespace namespace = Namespace.descriptor(descriptor);
        TypeParameterized type = this.environment.lookup(namespace).map(ClassNode::parameterizedType).orElse(Types.parameterized()
                .namespace(namespace)
                .superclass(Types.OBJECT)
                .addInterface(Types.parameterized(Annotation.class).withArguments()).build());

        return new AnnotationNodeVisitor(a -> this.node.annotations().add(a), type, this.environment);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Namespace namespace = Namespace.descriptor(descriptor);
        Verification<ClassNode> fieldType = this.environment.lookup(namespace);
        this.verification.child(fieldType);

        if (fieldType.success() && fieldType.value().isPresent()) {
            FieldNode node = new FieldNode(name, this.node, fieldType.value().get().type());
            node.modifiers().set(Modifiers.fromBits(access, ModifierTarget.FIELD));
            this.node.addField(node);

            if (node.modifiers().has(Modifier.STATIC) && value != null) {
                node.setValue(Nodes.constant(value));
            }

            if (signature != null) {
                TypeSignatureVisitor visitor = new TypeSignatureVisitor(v -> {
                    this.verification.child(v);
                    if (v.success() && v.value().isPresent()) {
                        node.setType(v.value().get());
                    }
                }, this.node, this.environment);

                SignatureReader reader = new SignatureReader(signature);
                reader.acceptType(visitor);
                visitor.visitFinish();
            }
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodNode node = new MethodNode(name, this.node);
        this.node.addMethod(node);

        node.modifiers().set(Modifiers.fromBits(access, ModifierTarget.METHOD));

        Type asmMethod = Type.getMethodType(descriptor);

        Namespace retName = Namespace.descriptor(asmMethod.getReturnType().getDescriptor());
        List<Namespace> paramNames = Stream.of(asmMethod.getArgumentTypes()).map(Type::getDescriptor).map(Namespace::descriptor).collect(Collectors.toList());
        List<Namespace> exceptionNames = Stream.of(exceptions == null ? new String[0] : exceptions).map(Namespace::internal).collect(Collectors.toList());

        Verification<ClassNode> retClass = this.environment.lookup(retName);
        this.verification.child(retClass);

        List<Verification<ClassNode>> paramClasses = paramNames.stream().map(n -> {
            Verification<ClassNode> paramClass = this.environment.lookup(n);
            this.verification.child(paramClass);
            return paramClass;
        }).toList();

        List<Verification<ClassNode>> exceptionNodes = exceptionNames.stream().map(n -> {
            Verification<ClassNode> exc = this.environment.lookup(n);
            this.verification.child(exc);
            return exc;
        }).toList();

        if (retClass.value().isPresent() &&
                paramClasses.stream().allMatch(v -> v.value().isPresent()) &&
                exceptionNodes.stream().allMatch(v -> v.value().isPresent())) {
            int param = 0;

            node.setReturnType(retClass.value().get().type());
            exceptionNodes.forEach(v -> node.addException(v.value().get().type()));

            for (Verification<ClassNode> v : paramClasses) {
                node.addParameter(new ParameterNode(v.value().get().type(), "arg" + param));
                param++;
            }
        }

        node.setErased(node.type());

        if (signature != null) {
            MethodSignatureVisitor visitor = new MethodSignatureVisitor(v -> {
                this.verification.child(v);
                if (v.success() && v.value().isPresent()) {
                    node.setType(v.value().get());
                }
            }, node, this.environment);

            SignatureReader reader = new SignatureReader(signature);
            reader.accept(visitor);
            visitor.visitFinish();
        }

        return new MethodNodeVisitor(node, this.environment);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        Namespace namespace = Namespace.internal(owner);
        Verification<ClassNode> lookup = this.environment.lookup(namespace);
        this.verification.child(lookup);

        if (lookup.success() && lookup.value().isPresent()) {
            ClassNode node = lookup.value().get();
            this.node.setOuterClass(node);

            if (name != null && descriptor != null) {
                Optional<MethodNode> opt = node.methods().stream().filter(m -> m.name().equals(name) && m.erased().descriptor().equals(descriptor)).findFirst();
                opt.ifPresent(methodNode -> {
                    this.node.setOuterMethod(methodNode);
                    methodNode.addInnerClass(this.node);
                });
            }
        }
    }

    @Override
    public void visitNestHost(String nestHost) {
        Namespace namespace = Namespace.internal(nestHost);
        Verification<ClassNode> lookup = this.environment.lookup(namespace);
        this.verification.child(lookup);

        if (lookup.success() && lookup.value().isPresent()) {
            this.node.setNestHost(lookup.value().get());
        }
    }

    @Override
    public void visitNestMember(String nestMember) {
        this.delay.add(() -> {
            Namespace namespace = Namespace.internal(nestMember);
            Verification<ClassNode> lookup = this.environment.lookup(namespace);
            this.verification.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                this.node.addNestClass(lookup.value().get());
            }
        });
    }

    private int anon = 0;

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        this.delay.add(() -> {
            Namespace namespace = Namespace.internal(name);
            Verification<ClassNode> lookup = this.environment.lookup(namespace);
            this.verification.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                InnerClassNode node = new InnerClassNode(lookup.value().get(), innerName == null ? String.valueOf(anon++) : innerName, innerName == null);
                node.modifiers().set(Modifiers.fromBits(access));
                this.node.addInnerClass(node);
            }
        });
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        this.node.addPermittedSubclass(Namespace.internal(permittedSubclass));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.delay.forEach(Runnable::run);
    }

    public Verification<ClassNode> verification() {
        return this.verification.andChildren().build();
    }
}
