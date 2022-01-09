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

public class Add extends BinaryOperator {

    public Add(Node left, Node right) {
        super("+", left, right);
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal a = this.left.type().erasure();
        TypeInformal b = this.right.type().erasure();

        if ((a.equals(Types.STRING) || (Types.unbox(a).isPrimitive() && TypeUtil.isNumeric(Types.unbox(a)))) &&
                (b.equals(Types.STRING) || (Types.unbox(b).isPrimitive() && TypeUtil.isNumeric(Types.unbox(b))))) {
            if (a.equals(Types.STRING) || b.equals(Types.STRING)) {
                return Verification.success(this, Types.STRING);
            } else {
                TypeInformal widest = TypeUtil.widestPrimitive(Types.unbox(a), Types.unbox(b));
                this.left = Nodes.convert(this.left, widest);
                this.right = Nodes.convert(this.right, widest);
                return Verification.success(this, widest);
            }
        }

        return Verification.error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator + to types %s and %s", a, b);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        TypeInformal a = this.left.type();
        TypeInformal b = this.left.type();

        if (a.equals(Types.STRING) || b.equals(Types.STRING)) {
            if (a.equals(Types.STRING) && b.equals(Types.STRING)) {
                this.right.apply(adapter, environment, context);
                this.left.apply(adapter, environment, context);
            } else if (a.equals(Types.STRING)) {
                this.right.apply(adapter, environment, context);
                adapter.invokestatic(Types.STRING.internalName(), "valueOf", Types.method()
                        .returnType(Types.STRING)
                        .addParameter(b.isPrimitive() ? b : Types.OBJECT).build().descriptor(), false);
                this.left.apply(adapter, environment, context);
            } else {
                this.right.apply(adapter, environment, context);
                this.left.apply(adapter, environment, context);
                adapter.invokestatic(Types.STRING.internalName(), "valueOf", Types.method()
                        .returnType(Types.STRING)
                        .addParameter(a.isPrimitive() ? a : Types.OBJECT).build().descriptor(), false);
            }

            adapter.invokevirtual(Types.STRING.internalName(), "concat", Types.method()
                    .returnType(Types.STRING)
                    .addParameter(Types.STRING).build().descriptor(), false);
        } else {
            this.left.apply(adapter, environment, context);
            this.right.apply(adapter, environment, context);
            adapter.add(TypeUtil.asmType(this.type()));
        }
    }
}
