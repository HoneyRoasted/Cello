package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class BooleanAnd extends AbstractNode implements BooleanValue {
    @Child
    private Convert left;
    @Child
    private Convert right;

    public BooleanAnd(Node left, Node right) {
        this.left = Nodes.convert(left, Types.BOOLEAN);
        this.right = Nodes.convert(right, Types.BOOLEAN);
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return Verification.success(this, Types.BOOLEAN);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        BooleanValue.apply(this, adapter, environment, context);
    }


    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label end = new Label();
        jump(ifTrue, end, adapter, environment, context);
        adapter.mark(end);
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.jumpIfFalse(ifFalse, adapter, environment, context);
        this.right.jumpIfFalse(ifFalse, adapter, environment, context);
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.jumpIfFalse(ifFalse, adapter, environment, context);
        this.right.jump(ifTrue, ifFalse, adapter, environment, context);
    }
}
