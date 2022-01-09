package honeyroasted.cello.node.instruction.operator;

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

public abstract class BinaryOperator extends AbstractNode implements Node {
    private String op;

    @Child(order = Child.BOTH)
    protected Node left;
    @Child(order = Child.BOTH)
    protected Node right;

    public BinaryOperator(String op, Node left, Node right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.left.type().erasure();
        TypeInformal b = this.right.type().erasure();
        if (Types.unbox(a).isPrimitive() && TypeUtil.isNumeric(Types.unbox(a)) &&
                Types.unbox(b).isPrimitive() && TypeUtil.isNumeric(Types.unbox(b))) {
            TypeInformal widest = TypeUtil.widestPrimitive(Types.unbox(a), Types.unbox(b));
            this.left = Nodes.convert(this.left, widest);
            this.right = Nodes.convert(this.right, widest);
            return Verification.success(this, widest);
        }

        return Verification.error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator %s to types %s and %s", this.op, a, b);
    }
}