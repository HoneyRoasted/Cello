package honeyroasted.cello.environment.bytecode.visitor;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.visitor.annotation.AnnotationNodeVisitor;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Opcodes.ASM9;

public class FieldNodeVisitor extends FieldVisitor {

    private FieldNode node;
    private Environment environment;

    public FieldNodeVisitor(FieldNode node, Environment environment) {
        super(ASM9);
        this.node = node;
        this.environment = environment;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Namespace namespace = Namespace.descriptor(descriptor);
        TypeParameterized type = this.environment.lookup(namespace).map(ClassNode::type).orElse(Types.parameterized()
                .namespace(namespace)
                .superclass(Types.OBJECT)
                .addInterface(Types.parameterized(Annotation.class).withArguments()).build());

        return new AnnotationNodeVisitor(a -> this.node.annotations().add(a), type, this.environment);
    }
}
