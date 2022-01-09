package honeyroasted.cello.node.instruction.operator.bitwise;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class BitwiseAnd extends AbstractNode implements Node, BooleanValue {
    @Child(order = Child.BOTH)
    private Node left;
    @Child(order = Child.BOTH)
    private Node right;

    public BitwiseAnd(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.left.type();
        TypeInformal b = this.right.type();

        if (a.isAssignableTo(Types.BOOLEAN) && b.isAssignableTo(Types.BOOLEAN)) {
            this.left = Nodes.convert(this.left, Types.BOOLEAN);
            this.right = Nodes.convert(this.right, Types.BOOLEAN);

            return Verification.success(this, Types.BOOLEAN);
        } else if ((a.isAssignableTo(Types.INT) || a.isAssignableTo(Types.LONG) &&
                (b.isAssignableTo(Types.INT) || b.isAssignableTo(Types.LONG)))) {
            TypeInformal widest = TypeUtil.widestPrimitive(a, b);

            this.left = Nodes.convert(this.left, widest);
            this.right = Nodes.convert(this.right, widest);

            return Verification.success(this, widest);
        }

        return Verification.error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator %s to types %s and %s", "&", a, b);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.type().equals(Types.BOOLEAN)) {
            BooleanValue.apply(this, adapter, environment, context);
        } else {
            adapter.and(TypeUtil.asmType(this.type()));
        }
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label end = new Label();
        jump(ifTrue, end, adapter, environment, context);
        adapter.mark(end);
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label leftTrue = new Label();

        if (this.left instanceof BooleanValue bop) {
            bop.jumpIfTrue(leftTrue, adapter, environment, context);
        } else {
            this.left.apply(adapter, environment, context);
            adapter.ifne(leftTrue);
        }

        if (this.right instanceof BooleanValue bop) {
            bop.jump(ifFalse, ifFalse, adapter, environment, context);

            adapter.mark(leftTrue);
            bop.jumpIfFalse(ifFalse, adapter, environment, context);
        } else {
            this.right.toUntyped().apply(adapter, environment, context);
            adapter.goTo(ifFalse);

            adapter.mark(leftTrue);
            this.right.apply(adapter, environment, context);
            adapter.ifeq(ifFalse);
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label leftTrue = new Label();

        if (this.left instanceof BooleanValue bop) {
            bop.jumpIfTrue(leftTrue, adapter, environment, context);
        } else {
            this.left.apply(adapter, environment, context);
            adapter.ifne(leftTrue);
        }

        if (this.right instanceof BooleanValue bop) {
            bop.jump(ifFalse, ifFalse, adapter, environment, context);

            adapter.mark(leftTrue);
            bop.jump(ifTrue, ifFalse, adapter, environment, context);
        } else {
            this.right.toUntyped().apply(adapter, environment, context);
            adapter.goTo(ifFalse);

            adapter.mark(leftTrue);
            this.right.apply(adapter, environment, context);
            adapter.ifeq(ifFalse);
            adapter.goTo(ifTrue);
        }
    }
}