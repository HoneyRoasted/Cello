package honeyroasted.cello.environment.bytecode;

import honeyroasted.cello.environment.AbstractCachingEnvironment;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.provider.BytecodeProvider;
import honeyroasted.cello.environment.bytecode.signature.TypeVarScope;
import honeyroasted.cello.environment.bytecode.visitor.ClassNodeVisitor;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BytecodeEnvironment extends AbstractCachingEnvironment {
    private List<BytecodeProvider> providers;

    public BytecodeEnvironment(Environment parent, List<BytecodeProvider> providers) {
        super(parent);
        this.providers = providers;
    }

    public BytecodeEnvironment(List<BytecodeProvider> providers) {
        this.providers = providers;
    }

    public BytecodeEnvironment(Environment parent) {
        this(parent, new ArrayList<>());
    }

    public BytecodeEnvironment() {
        this(new ArrayList<>());
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
                    .map(c -> new ClassNode(Types.parameterized()
                            .namespace(namespace)
                            .superclass(Types.OBJECT)
                            .build())
                            .setSuperclass(c).addField(new FieldNode("length", Types.INT)));
        }

        for (BytecodeProvider provider : this.providers) {
            Optional<byte[]> cls = provider.provide(namespace);
            if (cls.isPresent()) {
                byte[] arr = cls.get();

                TypeParameterized type = new TypeParameterized(namespace);
                ClassNode node = new ClassNode(type);
                cache(namespace, node);

                ClassNodeVisitor visitor = new ClassNodeVisitor(node, this, new TypeVarScope());
                ClassReader reader = new ClassReader(arr);
                reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                Verification<ClassNode> verification = visitor.verification();

                if (!verification.success()) {
                    remove(verification.value());
                }

                return verification;
            }
        }

        return Verification.<ClassNode>builder()
                .typeNotFoundError(namespace)
                .build();
    }

    @Override
    protected Verification<ClassNode> performArrayLookup(ClassNode element, Namespace namespace) {
        return lookup(Types.OBJECT)
                .map(c -> new ClassNode(Types.parameterized()
                        .namespace(namespace)
                        .superclass(Types.OBJECT)
                        .build())
                        .setSuperclass(c).addField(new FieldNode("length", Types.INT)));
    }
}
