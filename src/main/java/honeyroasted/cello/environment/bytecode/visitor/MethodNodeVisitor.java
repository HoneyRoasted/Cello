package honeyroasted.cello.environment.bytecode.visitor;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.visitor.annotation.AnnotationNodeVisitor;
import honeyroasted.cello.node.modifier.ModifierTarget;
import honeyroasted.cello.node.modifier.Modifiers;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.node.structure.ParameterNode;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Opcodes.*;

public class MethodNodeVisitor extends MethodVisitor {

    private MethodNode node;
    private Environment environment;

    public MethodNodeVisitor(MethodNode node, Environment environment) {
        super(ASM9);
        this.node = node;
        this.environment = environment;
    }

    private int param = 0;

    @Override
    public void visitParameter(String name, int access) {
        if (this.param < this.node.parameters().size()) {
            ParameterNode node = this.node.parameters().get(this.param);
            node.setName(name == null ? "arg" + this.param : name);
            node.modifiers().set(Modifiers.fromBits(access, ModifierTarget.PARAMETER));
        }

        this.param++;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        if (parameter < this.node.parameters().size()) {
            ParameterNode node = this.node.parameters().get(parameter);

            Namespace namespace = Namespace.descriptor(descriptor);
            TypeParameterized type = this.environment.lookup(namespace).map(ClassNode::parameterizedType).orElse(Types.parameterized()
                    .namespace(namespace)
                    .superclass(Types.OBJECT)
                    .addInterface(Types.parameterized(Annotation.class).withArguments()).build());

            return new AnnotationNodeVisitor(a -> node.annotations().add(a), type, this.environment);
        }
        return null;
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



}
