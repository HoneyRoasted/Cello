package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.method.TypeMethodFilled;
import honeyroasted.javatype.method.TypeMethodParameterized;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.commons.InstructionAdapter;

import java.lang.invoke.MethodType;

public class TypeConstant extends AbstractNode implements Constant<Type> {
    private Type value;

    public TypeConstant(Type value) {
        this.value = value;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (this.value instanceof TypeMethodParameterized || this.value instanceof TypeMethodFilled) {
            return Verification.success(this, Types.type(MethodType.class));
        } else if (this.value instanceof TypeInformal inf) {
            return Verification.success(this, Types.parameterized(Class.class).withArguments(inf));
        } else if (this.value instanceof TypeParameterized prm) {
            return Verification.success(this, Types.parameterized(Class.class).withArguments(prm.withArguments()));
        } else {
            return Verification.success(this, Types.parameterized(Class.class).withArguments(Types.wild().build()));
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.tconst(TypeUtil.asmType(this.value));
    }

    @Override
    public Type value() {
        return this.value;
    }

}
