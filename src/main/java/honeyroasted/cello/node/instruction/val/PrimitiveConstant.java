package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class PrimitiveConstant extends AbstractNode implements Constant<Object> {
    private Object constant;

    public PrimitiveConstant(Object constant) {
        this.constant = constant;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal type;

        if (this.constant instanceof Boolean) {
            type = Types.BOOLEAN;
        } else if (this.constant instanceof Character) {
            type = Types.CHAR;
        } else if (this.constant instanceof String) {
            type = Types.STRING;
        } else if (this.constant instanceof Byte) {
            type = Types.BYTE;
        } else if (this.constant instanceof Short) {
            type = Types.SHORT;
        } else if (this.constant instanceof Integer) {
            type = Types.INT;
        } else if (this.constant instanceof Long) {
            type = Types.LONG;
        } else if (this.constant instanceof Float) {
            type = Types.FLOAT;
        } else if (this.constant instanceof Double) {
            type = Types.DOUBLE;
        } else if (this.constant == null) {
            type = expected() == null ? Types.OBJECT : expected();
        } else {
            return Verification.error(this, Verify.Code.INVALID_CONSTANT_ERROR, "Invalid primitive constant type '%s'", this.constant.getClass().getName());
        }

        return Verification.success(this, type);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.constant instanceof Boolean bln) {
            adapter.iconst(bln ? 1 : 0);
        } else if (this.constant instanceof Character chr) {
            adapter.iconst(chr);
        } else if (this.constant instanceof String str) {
            adapter.aconst(str);
        } else if (this.constant instanceof Long lng) {
            adapter.lconst(lng);
        } else if (this.constant instanceof Float flt) {
            adapter.fconst(flt);
        } else if (this.constant instanceof Double dbl) {
            adapter.dconst(dbl);
        } else if (this.constant instanceof Number nmb) {
            adapter.iconst(nmb.intValue());
        } else if (this.constant == null) {
            adapter.aconst(null);
        }
    }

    @Override
    public Object value() {
        return this.value();
    }
}
