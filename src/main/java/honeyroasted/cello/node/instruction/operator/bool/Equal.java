package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class Equal extends AbstractNode implements BooleanValue {
    @Child(order = Child.BOTH)
    private Node left;
    @Child(order = Child.BOTH)
    private Node right;

    public Equal(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.left.type().erasure();
        TypeInformal b = this.right.type().erasure();

        if (a.isPrimitive() || b.isPrimitive()) {
            TypeInformal widest = TypeUtil.widestPrimitive(Types.unbox(a), Types.unbox(b));

            this.left = Nodes.convert(this.left, widest);
            this.right = Nodes.convert(this.right, widest);
        }

        return Verification.success(this, Types.BOOLEAN);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.left.type().isPrimitive()) {
            if (this.left.type().equals(Types.LONG)) {
                this.left.apply(adapter, environment, context);
                this.right.apply(adapter, environment, context);
                adapter.lcmp();
            } else if (!TypeUtil.isInteger32(this.left.type())) {
                this.left.apply(adapter, environment, context);
                this.right.apply(adapter, environment, context);
                adapter.cmpg(TypeUtil.asmType(this.left.type()));
            } else {
                BooleanValue.apply(this, adapter, environment, context);
            }
        } else {
            BooleanValue.apply(this, adapter, environment, context);
        }
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        if (this.left.type().isPrimitive()) {
            if (TypeUtil.isInteger32(this.left.type())) {
                adapter.ificmpeq(ifTrue);
            } else if (this.left.type().equals(Types.LONG)) {
                adapter.lcmp();
                adapter.ifeq(ifTrue);
            } else {
                adapter.cmpg(TypeUtil.asmType(this.left.type()));
                adapter.ifeq(ifTrue);
            }
        } else {
            adapter.ifacmpeq(ifTrue);
        }
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        if (this.left.type().isPrimitive()) {
            if (TypeUtil.isInteger32(this.left.type())) {
                adapter.ificmpne(ifFalse);
            } else if (this.left.type().equals(Types.LONG)) {
                adapter.lcmp();
                adapter.ifne(ifFalse);
            } else {
                adapter.cmpg(TypeUtil.asmType(this.left.type()));
                adapter.ifne(ifFalse);
            }
        } else {
            adapter.ifacmpne(ifFalse);
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        jumpIfTrue(ifTrue, adapter, environment, context);
        adapter.goTo(ifFalse);
    }
}
