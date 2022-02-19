package honeyroasted.cello.environment.bytecode.visitor.annotation;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.AnnotationVisitor;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM9;

public class AnnotationArrayNodeVisitor extends AnnotationVisitor {

    private AnnotationValue.Array array = new AnnotationValue.Array();
    private Consumer<AnnotationValue.Array> end;
    private Environment environment;

    public AnnotationArrayNodeVisitor(Consumer<AnnotationValue.Array> end, Environment environment) {
        super(ASM9);
        this.end = end;
        this.environment = environment;
    }

    @Override
    public void visit(String name, Object value) {
        this.array.add(Nodes.constant(value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        Namespace namespace = Namespace.descriptor(descriptor);
        TypeParameterized type = new TypeParameterized(namespace);

        Verification<ClassNode> lookup = this.environment.lookup(namespace);
        if (lookup.success() && lookup.value().isPresent()) {
            type = lookup.value().get().parameterizedType();
        } else {
            Types.parameterized()
                    .namespace(namespace)
                    .superclass(Types.parameterized(Enum.class).withArguments(type.withArguments()))
                    .build(type);
        }

        this.array.add(new AnnotationValue.Enum(type, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        Namespace namespace = Namespace.descriptor(descriptor);
        TypeParameterized type = this.environment.lookup(namespace).map(ClassNode::parameterizedType).orElse(Types.parameterized()
                .namespace(namespace)
                .superclass(Types.OBJECT)
                .addInterface(Types.parameterized(Annotation.class).withArguments()).build());

        return new AnnotationNodeVisitor(a -> this.array.add(a), type, this.environment);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.end.accept(this.array);
    }
}