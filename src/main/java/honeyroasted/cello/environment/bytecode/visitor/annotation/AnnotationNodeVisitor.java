package honeyroasted.cello.environment.bytecode.visitor.annotation;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.annotation.AnnotationNode;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.AnnotationVisitor;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

public class AnnotationNodeVisitor extends AnnotationVisitor {
    private Consumer<AnnotationNode> end;
    private Environment environment;

    private AnnotationNode node;

    public AnnotationNodeVisitor(Consumer<AnnotationNode> end, TypeParameterized type, Environment environment) {
        super(ASM9);
        this.end = end;
        this.node = new AnnotationNode(type);
        this.environment = environment;
    }

    @Override
    public void visit(String name, Object value) {
        this.node.put(name, Nodes.constant(value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        this.node.put(name, new AnnotationValue.Enum(Namespace.descriptor(descriptor), value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        Namespace namespace = Namespace.descriptor(descriptor);
        TypeParameterized type = this.environment.lookup(namespace).map(ClassNode::type).orElse(Types.parameterized()
                .namespace(namespace)
                .superclass(Types.OBJECT)
                .addInterface(Types.parameterized(Annotation.class).withArguments()).build());

        return new AnnotationNodeVisitor(a -> this.node.put(name, a), type, this.environment);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationArrayNodeVisitor(a -> this.node.put(name, a), this.environment);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.end.accept(this.node);
    }
}
