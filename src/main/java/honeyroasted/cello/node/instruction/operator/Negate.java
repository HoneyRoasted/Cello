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
import org.objectweb.asm.commons.InstructionAdapter;

public class Negate extends AbstractNode implements Node {
    @Child(order = Child.BOTH)
    private Node value;

    public Negate(Node value) {
        this.value = value;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.value.type().erasure();
        if (Types.unbox(a).isPrimitive() && TypeUtil.isNumeric(Types.unbox(a))) {
            this.value = Nodes.convert(this.value, Types.unbox(a));
        }

        return Verification.error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator %s to type %s", "-", a);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);
        adapter.neg(TypeUtil.asmType(this.type()));
    }

}
