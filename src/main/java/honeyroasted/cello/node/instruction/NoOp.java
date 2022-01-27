package honeyroasted.cello.node.instruction;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class NoOp extends AbstractNode implements Node {
    private TypeInformal type;

    public NoOp(TypeInformal type) {
        this.type = type;
    }

    public NoOp() {
        this(Types.VOID);
    }

    @Child(order = Child.POST)
    private Node constant;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (!this.type.equals(Types.VOID)) {
            this.constant = Nodes.defaultValue(this.type);
        }
        return Verification.success(this, this.type);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.constant != null) {
            this.constant.apply(adapter, environment, context);
        }
    }

}
