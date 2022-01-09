package honeyroasted.cello.node.instruction;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public interface BooleanValue extends Node {

    void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context);

    void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context);

    void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context);

    static void apply(BooleanValue value, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label ifFalse = new Label();
        Label end = new Label();

        value.jumpIfFalse(ifFalse, adapter, environment, context);

        adapter.iconst(1);
        adapter.goTo(end);
        adapter.mark(ifFalse);
        adapter.iconst(0);
        adapter.mark(end);
    }

}
