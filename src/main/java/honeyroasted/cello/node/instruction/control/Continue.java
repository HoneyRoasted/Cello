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

public class Continue extends AbstractNode implements Node {
    private String name;

    public Continue(String name) {
        this.name = name;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (context.scope().fetchControl(Control.Kind.CONTINUE, this.name).isPresent()) {
            return Verification.success(this, Types.VOID);
        } else {
            if (context.scope().fetchControl(Control.Kind.CONTINUE).isPresent()) {
                return Verification.error(this, Verify.Code.LABEL_NOT_FOUND, "no block named %s found", this.name);
            } else {
                return Verification.error(this, Verify.Code.CONTROL_FLOW_ERROR, "continue statement not allowed here");
            }        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.goTo(context.scope().fetchControl(Control.Kind.CONTINUE, this.name).get().label());
    }
}
