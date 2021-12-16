package honeyroasted.cello.node.ast.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class Constant extends AbstractPropertyHolder implements TypedNode<Constant, Constant> {
    private Object object;
    private TypeInformal type;

    public Constant(Object object) {
        this.object = object;
    }

    @Override
    public void provideExpected(TypeInformal type) {
        if (this.object == null && !type.isPrimitive()) {
            this.type = type;
        }
    }

    @Override
    public Verification<Constant> verify(Environment environment, LocalScope localScope) {
        if (this.object == null) {
            if (this.type == null) {
                this.type = Types.OBJECT;
            }
            return Verification.success(this);
        } else {
            if (this.object instanceof Byte) {
                this.type = Types.BYTE;
                return Verification.success(this);
            } else if (this.object instanceof Short) {
                this.type = Types.SHORT;
                return Verification.success(this);
            } else if (this.object instanceof Character) {
                this.type = Types.CHAR;
                return Verification.success(this);
            } else if (this.object instanceof Long) {
                this.type = Types.LONG;
                return Verification.success(this);
            } else if (this.object instanceof Float) {
                this.type = Types.FLOAT;
                return Verification.success(this);
            } else if (this.object instanceof Double) {
                this.type = Types.DOUBLE;
                return Verification.success(this);
            } else if (this.object instanceof String) {
                this.type = Types.STRING;
                return Verification.success(this);
            } else {
                return Verification.builder(this)
                        .invalidConstant(this.object.getClass())
                        .build();
            }
        }
    }

    @Override
    public TypeInformal type() {
        return this.type;
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        if (this.object == null) {
            adapter.aconst(null);
        } else if (this.object instanceof String str) {
            adapter.aconst(str);
        } else if (this.object instanceof Float flt) {
            adapter.fconst(flt);
        } else if (this.object instanceof Double dbl) {
            adapter.dconst(dbl);
        } else if (this.object instanceof Long lng) {
            adapter.lconst(lng);
        } else if (this.object instanceof Character chr) {
            adapter.iconst(chr);
        } else if (this.object instanceof Boolean bln) {
            adapter.iconst(bln ? 1 : 0);
        } else if (this.object instanceof Number nmb) {
            adapter.iconst(nmb.intValue());
        }
    }
}
