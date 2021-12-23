package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class BooleanNot extends AbstractPropertyHolder implements BooleanOperator<BooleanNot, BooleanNot> {
    private TypedNode arg;

    public BooleanNot(TypedNode arg) {
        this.arg = arg;
    }

    @Override
    public Verification<BooleanNot> verify(Environment environment, LocalScope localScope) {
        return Verification.builder(this)
                .child(this.arg.verify(environment, localScope))
                .andChildren()
                .build();
    }

    @Override
    public BooleanNot preprocess() {
        this.arg = this.arg instanceof BooleanOperator ? this.arg.preprocessFully() :
                Nodes.convert(this.arg, Types.BOOLEAN).preprocessFully();
        return this;
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        if (this.arg instanceof BooleanOperator bop) {
            bop.jumpIfFalse(ifTrue, adapter, environment, localScope);
        } else {
            this.arg.apply(adapter, environment, localScope);
            adapter.ifeq(ifTrue);
        }
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        if (this.arg instanceof BooleanOperator bop) {
            bop.jumpIfTrue(ifFalse, adapter, environment, localScope);
        } else {
            this.arg.apply(adapter, environment, localScope);
            adapter.ifne(ifFalse);
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        if (this.arg instanceof BooleanOperator bop) {
            bop.jump(ifFalse, ifTrue, adapter, environment, localScope);
        } else {
            this.arg.apply(adapter, environment, localScope);
            adapter.ifeq(ifTrue);
            adapter.goTo(ifFalse);
        }
    }
}
