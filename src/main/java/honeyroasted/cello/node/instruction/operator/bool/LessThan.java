package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.operator.BinaryOperator;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class LessThan extends BinaryOperator implements BooleanValue {
    private boolean includeEquals;

    public LessThan(Node left, Node right, boolean includeEquals) {
        super(includeEquals ? "<=" : "<", left, right);
        this.includeEquals = includeEquals;
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (TypeUtil.isInteger32(this.type())) {
            BooleanValue.apply(this, adapter, environment, context);
        } else {
            this.left.apply(adapter, environment, context);
            this.right.apply(adapter, environment, context);
            if (this.type().equals(Types.LONG)) {
                adapter.lcmp();
            } else {
                adapter.cmpg(TypeUtil.asmType(this.type()));
            }
        }
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        if (TypeUtil.isInteger32(this.type())) {
            if (this.includeEquals) {
                adapter.ificmple(ifTrue);
            } else {
                adapter.ificmplt(ifTrue);
            }
        } else {
            if (this.type().equals(Types.LONG)) {
                adapter.lcmp();
            } else {
                adapter.cmpg(TypeUtil.asmType(this.type()));
            }

            if (this.includeEquals) {
                adapter.ifle(ifTrue);
            } else {
                adapter.iflt(ifTrue);
            }
        }
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        if (TypeUtil.isInteger32(this.type())) {
            if (this.includeEquals) {
                adapter.ificmpgt(ifFalse);
            } else {
                adapter.ificmpge(ifFalse);
            }
        } else {
            if (this.type().equals(Types.LONG)) {
                adapter.lcmp();
            } else {
                adapter.cmpl(TypeUtil.asmType(this.type()));
            }

            if (this.includeEquals) {
                adapter.ifgt(ifFalse);
            } else {
                adapter.ifge(ifFalse);
            }
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        jumpIfTrue(ifTrue, adapter, environment, context);
        adapter.goTo(ifFalse);
    }

}