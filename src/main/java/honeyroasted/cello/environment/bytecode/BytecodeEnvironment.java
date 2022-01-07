package honeyroasted.cello.environment.bytecode;

import honeyroasted.cello.environment.AbstractCachingEnvironment;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.provider.BytecodeProvider;
import honeyroasted.cello.environment.TypeVarScope;
import honeyroasted.cello.environment.bytecode.visitor.ClassNodeVisitor;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.ClassReader;

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

    @Override
    protected Verification<ClassNode> performLookup(Namespace namespace) {
        if (namespace.isPrimitive()) {
            return Verification.success(new ClassNode(Types.parameterized()
                    .namespace(namespace)
                    .build()));
        } else if (namespace.isArray()) {
            return lookup(Types.OBJECT)
                    .map(c -> {
                        ClassNode node = new ClassNode(Types.parameterized()
                                .namespace(namespace)
                                .superclass(Types.OBJECT)
                                .build())
                                .setSuperclass(c);
                        node.addField(new FieldNode("length", node, Types.INT));
                        return node;
                    });
        }

        Verification.Builder<ClassNode> builder = Verification.builder();

        for (BytecodeProvider provider : this.providers) {
            Verification<byte[]> cls = provider.provide(namespace);
            builder.child(cls);
            if (cls.isPresent()) {
                byte[] arr = cls.value();

                TypeParameterized type = new TypeParameterized(namespace);
                ClassNode node = new ClassNode(type);
                cache(namespace, node);

                ClassNodeVisitor visitor = new ClassNodeVisitor(node, this);
                ClassReader reader = new ClassReader(arr);
                reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                Verification<ClassNode> verification = visitor.verification();
                builder.child(verification);

                if (!verification.isPresent()) {
                    remove(verification.value());
                } else {
                    builder.value(verification.value());
                    return builder.success(true).build();
                }
            }
        }

        return builder
                .typeNotFoundError(namespace)
                .build();
    }

}
