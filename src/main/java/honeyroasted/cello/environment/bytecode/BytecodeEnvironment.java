package honeyroasted.cello.environment.bytecode;

import honeyroasted.cello.environment.AbstractCachingEnvironment;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.provider.BytecodeProvider;
import honeyroasted.cello.environment.bytecode.visitor.ClassNodeVisitor;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.ClassReader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BytecodeEnvironment extends AbstractCachingEnvironment {
    private List<BytecodeProvider> providers;

    public BytecodeEnvironment(Environment parent, List<BytecodeProvider> providers) {
        super(parent);
        this.providers = providers;
    }

    public BytecodeEnvironment(List<BytecodeProvider> providers) {
        this.providers = providers;
    }

    public BytecodeEnvironment(Environment parent, BytecodeProvider... providers) {
        this(parent, new ArrayList<>(Arrays.asList(providers)));
    }

    public BytecodeEnvironment(BytecodeProvider... providers) {
        this(new ArrayList<>(Arrays.asList(providers)));
    }

    public List<BytecodeProvider> providers() {
        return providers;
    }

    private Verification<ClassNode> buildArrayClass(ClassNode node, ClassNode element) {
        Namespace namespace = Namespace.of(element.parameterizedType().namespace().packageName(), element.parameterizedType().namespace().className() + "[]");

        VerificationBuilder<ClassNode> verification = Verification.builder();
        verification.source(this);

        Verification<ClassNode> object = lookup(Types.OBJECT);
        Verification<ClassNode> cloneable = lookup(Types.parameterized(Cloneable.class));
        Verification<ClassNode> serializable = lookup(Types.parameterized(Serializable.class));
        verification.children(object, cloneable, serializable);


        if (object.success() && object.value().isPresent() &&
                cloneable.success() && cloneable.value().isPresent() &&
                serializable.success() && serializable.value().isPresent()) {


            node.setParameterizedType(Types.parameterized()
                    .namespace(namespace)
                    .superclass(Types.OBJECT)
                    .addInterface(Types.parameterized(Cloneable.class).withArguments())
                    .addInterface(Types.parameterized(Serializable.class).withArguments())
                    .build());

            node.setSuperclass(object.value().get())
                    .addInterface(cloneable.value().get())
                    .addInterface(serializable.value().get())
                    .setArrayElement(element);

            verification.value(node);
        }

        return verification.andChildren().build();
    }

    @Override
    protected Verification<ClassNode> performLookup(Namespace namespace) {
        if (namespace.isPrimitive()) {
            return Verification.success(this, new ClassNode(Types.parameterized()
                    .namespace(namespace)
                    .build()));
        } else if (namespace.isArray()) {
            Namespace element = namespace.element();
            ClassNode node = new ClassNode(new TypeParameterized(element));
            cache(namespace, node);

            int depth = namespace.depth();

            VerificationBuilder<ClassNode> builder = Verification.builder();

            Verification<ClassNode> lookup = lookup(element);
            builder.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                ClassNode elementNode = lookup.value().get();
                for (int i = 0; i < depth; i++) {
                    Verification<ClassNode> array = buildArrayClass(node, elementNode);
                    builder.child(array);
                    if (array.success() && array.value().isPresent()) {
                        node = array.value().get();
                    }
                }
            }

            return builder.value(node).andChildren().build();
        } else {
            VerificationBuilder<ClassNode> builder = Verification.builder();
            builder.source(this);

            for (BytecodeProvider provider : this.providers) {
                Verification<byte[]> cls = provider.provide(namespace);
                builder.child(cls);
                if (cls.success() && cls.value().isPresent()) {
                    byte[] arr = cls.value().get();

                    TypeParameterized type = new TypeParameterized(namespace);
                    ClassNode node = new ClassNode(type);
                    cache(namespace, node);

                    ClassNodeVisitor visitor = new ClassNodeVisitor(node, this);
                    ClassReader reader = new ClassReader(arr);
                    reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    Verification<ClassNode> verification = visitor.verification();
                    builder.child(verification);

                    if (!verification.success() && verification.value().isPresent()) {
                        remove(verification.value().get());
                    } else {
                        builder.value(verification.value().get());
                        return builder.level(Verify.Level.SUCCESS).build();
                    }
                }
            }

            return builder.error(Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not resolve class '%s'", namespace.name()).build();
        }
    }

}
