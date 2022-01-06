package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.commons.InstructionAdapter;

public class InstanceOf extends AbstractPropertyHolder implements TypedNode<InstanceOf, InstanceOf> {
    private String patternVar;
    private TypedNode value;
    private TypeParameterized type;

    public InstanceOf(String patternVar, TypedNode value, TypeParameterized type) {
        this.patternVar = patternVar;
        this.value = value;
        this.type = type;
    }

    @Override
    public InstanceOf preprocess() {
        this.value = this.value.preprocessFully();
        return this;
    }

    @Override
    public Verification<InstanceOf> verify(Environment environment, CodeContext context) {
        Verification<?> verification = this.value.verify(environment, context);
        if (verification.success()) {
            if (this.value.type().isPrimitive()) {
                return Verification.builder(this)
                        .child(verification)
                        .invalidTypeError(this.value.type())
                        .build();
            } else if (this.type.isPrimitive()) {
                return Verification.builder(this)
                        .child(verification)
                        .invalidTypeError(this.type.withArguments())
                        .build();
            } else {
                if (this.patternVar != null) {
                    if (context.scope().has(this.patternVar)) {
                        return Verification.builder(this)
                                .child(verification)
                                .varAlreadyDefinedError(this.patternVar)
                                .build();
                    } else {
                        context.scope().define(this.patternVar, this.type.withArguments());
                    }
                }

                return Verification.builder(this)
                        .child(verification)
                        .build();
            }
        } else {
            return Verification.builder(this)
                    .child(verification)
                    .noChildError()
                    .build();
        }
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);
        if (this.patternVar != null) {
            adapter.dup();
        }
        adapter.instanceOf(TypeUtil.asmType(this.type));
        if(this.patternVar != null) {
            Var var = context.scope().define(this.patternVar, this.type.withArguments()).get();
            adapter.store(var.index(), TypeUtil.asmType(this.type));
        }
    }

    @Override
    public TypeInformal type() {
        return Types.BOOLEAN;
    }

}
