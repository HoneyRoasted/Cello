package honeyroasted.cello.node.instruction.operator.bitwise;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class RightShift extends AbstractNode implements Node {
    @Child(order = Child.BOTH)
    private Node left;
    @Child(order = Child.BOTH)
    private Node right;
    private boolean unsigned;

    public RightShift(Node left, Node right, boolean unsigned) {
        this.left = left;
        this.right = right;
        this.unsigned = unsigned;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.left.type();
        TypeInformal b = this.right.type();

        if ((a.isAssignableTo(Types.INT) || a.isAssignableTo(Types.LONG)) &&
                (b.isAssignableTo(Types.INT) || b.isAssignableTo(Types.LONG))) {

            TypeInformal type;

            if (a.isAssignableTo(Types.INT)) {
                this.left = Nodes.convert(this.left, Types.INT);
                type = Types.INT;
            } else {
                this.left = Nodes.convert(this.left, Types.LONG);
                type = Types.LONG;
            }

            this.right = Nodes.cast(this.right, Types.INT);

            return Verification.success(this, type);
        } else {
            return Verification.error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator %s to types %s and %s", this.unsigned ? ">>>" : ">>", a, b);
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.left.apply(adapter, environment, context);
        this.right.apply(adapter, environment, context);

        if (this.unsigned) {
            adapter.ushr(TypeUtil.asmType(this.type()));
        } else {
            adapter.shr(TypeUtil.asmType(this.type()));
        }
    }

}
