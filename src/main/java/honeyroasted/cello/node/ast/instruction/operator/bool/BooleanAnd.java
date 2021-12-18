package honeyroasted.cello.node.ast.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class BooleanAnd extends AbstractPropertyHolder implements BooleanOperator<BooleanAnd, BooleanAnd> {
    private List<TypedNode> arguments;

    public BooleanAnd(List<TypedNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public BooleanAnd preprocess() {
        this.arguments = this.arguments.stream().map(t -> t instanceof BooleanOperator ? t.preprocessFully() :
                Nodes.convert(t, Types.BOOLEAN).preprocessFully()).collect(Collectors.toList());
        return this;
    }

    @Override
    public Verification<BooleanAnd> verify(Environment environment, LocalScope localScope) {
        return Verification.builder(this)
                .children(this.arguments.stream().map(t -> t.verify(environment, localScope)).collect(Collectors.toList()))
                .andChildren()
                .build();
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        Label ifFalse = new Label();
        jump(ifTrue, ifFalse, adapter, environment, localScope);
        adapter.mark(ifFalse);
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        for (TypedNode arg : this.arguments) {
            if (arg instanceof BooleanOperator bop) {
                bop.jumpIfFalse(ifFalse, adapter, environment, localScope);
            } else {
                arg.apply(adapter, environment, localScope);
                adapter.ifeq(ifFalse);
            }
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        jumpIfFalse(ifFalse, adapter, environment, localScope);
        adapter.goTo(ifTrue);
    }
}
