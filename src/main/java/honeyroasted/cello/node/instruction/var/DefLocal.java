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

public class DefLocal extends AbstractNode implements Node {
    private TypeInformal type;
    private String name;

    @Child
    private Node value;

    public DefLocal(TypeInformal type, String name, Node value) {
        this.type = type;
        this.name = name;

        this.value = value == null ? null : Nodes.conversion(value, type);
    }

    public DefLocal(String name, Node value) {
        this.name = name;
        this.value = value;
    }

    public DefLocal(String name) {
        this.name = name;
    }

    public DefLocal(TypeInformal type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (context.scope().has(this.name)) {
            return Verification.error(this, Verify.Code.VAR_ALREADY_DEFINED_ERROR, "Variable '%s' is already defined in local scope", this.name);
        } else {
            if (this.type == null) {
                this.type = this.value == null ? Types.OBJECT : this.value.type();
            }

            Var var = context.scope().define(this.name, this.type);
            if (this.value != null) {
                var.setInitialized(true);
            }
            return Verification.success(this, Types.VOID);
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.value != null) {
            this.value.apply(adapter, environment, context);
        }
        Var var = context.scope().define(this.name, this.type);

        if (this.value != null) {
            adapter.store(var.index(), TypeUtil.asmType(this.type));
            var.setInitialized(true);
        }

        adapter.visitLocalVariable(this.name, this.type.descriptor(), this.type.signature(), context.scope().start(), context.scope().end(), var.index());
    }
}
