package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class SetLocal extends AbstractNode implements Node {
    private String name;
    @Child
    private Node value;
    private boolean dup;

    public SetLocal(String name, Node value, boolean dup) {
        this.name = name;
        this.value = Nodes.convert(value, (e, c) -> c.scope().fetch(this.name).map(Var::type).orElse(value.type()));
        this.dup = dup;
    }

    public SetLocal(String name, Node value) {
        this(name, value, true);
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        Optional<Var> opt = context.scope().fetch(this.name);
        if (opt.isPresent()) {
            opt.get().setInitialized(true);
            return Verification.success(this, this.dup ? opt.get().type() : Types.VOID);
        } else {
            return Verification.error(this, Verify.Code.VAR_NOT_FOUND_ERROR, "Variable '%s' not found in local scope", this.name);
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);

        if (this.dup) {
            int size = TypeUtil.size(this.value.type());
            if (size == 1) {
                adapter.dup();
            } else if (size == 2) {
                adapter.dup2();
            }
        }

        Var var = context.scope().fetch(this.name).get();
        var.setInitialized(true);

        adapter.store(var.index(), TypeUtil.asmType(var.type()));
    }

    @Override
    public Node toUntyped() {
        return new SetLocal(this.name, Nodes.unwrapConvert(this.value), false).withProperties(this.properties());
    }
}
