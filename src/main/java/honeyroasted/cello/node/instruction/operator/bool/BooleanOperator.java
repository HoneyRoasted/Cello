package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public interface BooleanOperator<T extends  BooleanOperator, K extends BooleanOperator> extends TypedNode<T, K> {

    void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context);

    void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context);

    void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context);

    @Override
    default TypeInformal type() {
        return Types.BOOLEAN;
    }

    @Override
    default void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label ifFalse = new Label();
        Label end = new Label();

        jumpIfFalse(ifFalse, adapter, environment, context);

        Nodes.constant(true).apply(adapter, environment, context);
        adapter.goTo(end);
        adapter.mark(ifFalse);
        Nodes.constant(false).apply(adapter, environment, context);
        adapter.mark(end);

    }

}
