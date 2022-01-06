package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class BooleanOr extends AbstractPropertyHolder implements BooleanOperator<BooleanOr, BooleanOr> {
    private List<TypedNode> arguments;

    public BooleanOr(List<TypedNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public BooleanOr preprocess() {
        this.arguments = this.arguments.stream().map(t -> t instanceof BooleanOperator ? t.preprocessFully() :
                Nodes.convert(t, Types.BOOLEAN).preprocessFully()).collect(Collectors.toList());
        return this;
    }

    @Override
    public Verification<BooleanOr> verify(Environment environment, CodeContext context) {
        return Verification.builder(this)
                .children(this.arguments.stream().map(t -> (Verification<?>) t.verify(environment, context)).collect(Collectors.toList()))
                .andChildren()
                .build();
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        for (TypedNode arg : this.arguments) {
            if (arg instanceof BooleanOperator bop) {
                bop.jumpIfTrue(ifTrue, adapter, environment, context);
            } else {
                arg.apply(adapter, environment, context);
                adapter.ifne(ifTrue);
            }
        }
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label ifTrue = new Label();
        jump(ifTrue, ifFalse, adapter, environment, context);
        adapter.mark(ifTrue);
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        jumpIfTrue(ifTrue, adapter, environment, context);
        adapter.goTo(ifFalse);
    }

}
