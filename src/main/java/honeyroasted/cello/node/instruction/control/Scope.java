package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class Scope extends AbstractNode implements Node {
    @Child(scope = Child.SUB_SCOPE)
    private Node child;

    public Scope(Node child) {
        this.child = child.toUntyped();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return Verification.success(this, Types.VOID);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        CodeContext child = context.childScope();
        adapter.visitLabel(child.scope().start());
        this.child.apply(adapter, environment, context);
        adapter.visitLabel(child.scope().end());
    }

}
