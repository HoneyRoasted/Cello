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

public class BooleanNot extends AbstractNode implements BooleanValue {
    @Child
    private Convert value;

    public BooleanNot(Node value) {
        this.value = Nodes.convert(value, Types.BOOLEAN);
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
        this.value.jumpIfFalse(ifTrue, adapter, environment, context);
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.jumpIfTrue(ifFalse, adapter, environment, context);
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.jump(ifFalse, ifTrue, adapter, environment, context);
    }
}
