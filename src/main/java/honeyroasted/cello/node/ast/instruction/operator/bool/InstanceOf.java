package honeyroasted.cello.node.ast.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
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
    public Verification<InstanceOf> verify(Environment environment, LocalScope localScope) {
        Verification<?> verification = this.value.verify(environment, localScope);
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
                    if (localScope.has(this.patternVar)) {
                        return Verification.builder(this)
                                .child(verification)
                                .varAlreadyDefinedError(this.patternVar)
                                .build();
                    } else {
                        localScope.define(this.patternVar, this.type.withArguments());
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
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.value.apply(adapter, environment, localScope);
        adapter.instanceOf(TypeUtil.asmType(this.type));
        localScope.define(this.patternVar, this.type.withArguments());
    }

    @Override
    public TypeInformal type() {
        return Types.BOOLEAN;
    }

}
