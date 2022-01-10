package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class InstanceOf extends AbstractNode implements Node {
    @Child
    private Node node;
    private TypeInformal type;
    private String var;

    public InstanceOf(Node node, TypeInformal type, String var) {
        this.node = node;
        this.type = type;
        this.var = var;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (this.var != null) {
            if (context.scope().has(this.var)) {
                return Verification.error(this, Verify.Code.VAR_ALREADY_DEFINED_ERROR, "Variable '%s' is already defined in local scope", this.var);
            }

            context.scope().define(this.var, this.type);
        }

        return Verification.success(this, Types.VOID);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.node.apply(adapter, environment, context);

        if (this.var != null) {
            adapter.dup();
        }

        adapter.instanceOf(TypeUtil.asmType(this.type));

        if (this.var != null) {
            Label end = new Label();
            adapter.dup();
            adapter.ifeq(end);

            adapter.swap();
            adapter.checkcast(TypeUtil.asmType(this.type));

            Var var = context.scope().define(this.var, this.type);
            adapter.store(var.index(), TypeUtil.asmType(this.type));

            adapter.mark(end);
        }
    }

}
