package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class GetLocal extends AbstractNode implements Node {
    private String name;

    public GetLocal(String name) {
        this.name = name;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        Optional<Var> opt = context.scope().fetch(this.name);
        if (opt.isPresent()) {
            if (opt.get().initialized()) {
                return Verification.success(this, opt.get().type());
            } else {
                return Verification.error(this, Verify.Code.VAR_NOT_FOUND_ERROR, "Variable '%s' in local scope was not initialized", this.name);
            }
        } else {
            return Verification.error(this, Verify.Code.VAR_NOT_FOUND_ERROR, "Variable '%s' not found in local scope", this.name);
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Var var = context.scope().fetch(this.name).get();
        adapter.load(var.index(), TypeUtil.asmType(var.type()));
    }

}
