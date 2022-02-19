package honeyroasted.cello.node.structure;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.verify.Verification;
import org.objectweb.asm.ClassVisitor;

public class ClassNodeHandler {
    private ClassNode node;

    public ClassNodeHandler(ClassNode node) {
        this.node = node;
    }

    public Verification<ClassNode> verify(Environment environment) {

        return Verification.success(this, this.node);
    }

    public void apply(Environment environment, ClassVisitor visitor) {

    }



}
