package honeyroasted.cello.node.ast.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public interface BooleanOperator<T extends  BooleanOperator, K extends BooleanOperator> extends TypedNode<T, K> {

    void jumpIfTrue(Label ifTrue);

    void jumpIfFalse(Label ifFalse);

    void jump(Label ifTrue, Label ifFalse);

    @Override
    default TypeInformal type() {
        return Types.BOOLEAN;
    }

    @Override
    default void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        Label ifFalse = new Label();
        Label end = new Label();

        jumpIfFalse(ifFalse);

        Nodes.constant(true).apply(adapter, environment, localScope);
        adapter.goTo(end);
        adapter.mark(ifFalse);
        Nodes.constant(false).apply(adapter, environment, localScope);
        adapter.mark(end);

    }

}
