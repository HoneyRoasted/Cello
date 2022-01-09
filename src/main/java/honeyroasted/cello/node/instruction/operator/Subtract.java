package honeyroasted.cello.node.instruction.operator;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import org.objectweb.asm.commons.InstructionAdapter;

public class Subtract extends BinaryOperator {

    public Subtract(Node left, Node right) {
        super("-", left, right);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        adapter.sub(TypeUtil.asmType(this.type()));
    }

}
