package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class Break extends AbstractNode implements Node {
    private String name;

    public Break(String name) {
        this.name = name;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (context.scope().fetchControl(Control.Kind.BREAK, this.name).isPresent()) {
            return Verification.success(this, Types.VOID);
        } else {
            if (context.scope().fetchControl(Control.Kind.BREAK).isPresent()) {
                return Verification.error(this, Verify.Code.CONTROL_FLOW_ERROR, "no block named %s found", this.name);
            } else {
                return Verification.error(this, Verify.Code.CONTROL_FLOW_ERROR, "break statement not allowed here");
            }
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.goTo(context.scope().fetchControl(Control.Kind.BREAK, this.name).get().label());
    }
}