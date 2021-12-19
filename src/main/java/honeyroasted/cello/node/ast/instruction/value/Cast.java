package honeyroasted.cello.node.ast.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public class Cast extends AbstractPropertyHolder implements TypedNode<Cast, Cast> {
    private TypedNode value;
    private BiFunction<Environment, LocalScope, TypeInformal> target;

    public Cast(TypedNode<?, ?> value, BiFunction<Environment, LocalScope, TypeInformal> target) {
        this.value = value;
        this.target = target;
    }

    private TypeInformal targetType;

    @Override
    public Cast preprocess() {
        this.value = this.value.preprocessFully();
        return this;
    }

    @Override
    public Verification<Cast> verify(Environment environment, LocalScope localScope) {
        TypeInformal target = this.target.apply(environment, localScope);
        this.targetType = target;
        this.value.provideExpected(target);

        Verification<TypedNode> verification = this.value.verify(environment, localScope);

        if (verification.success()) {
            TypeInformal source = this.value.type();
            if (!target.isAssignableTo(source)) {
                return Verification.builder(this)
                        .child(verification)
                        .typeError(target, source)
                        .build();
            } else {
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

        TypeInformal origin = this.value.type();
        TypeInformal target = this.targetType;

        if (origin.isAssignableTo(target)) {
            //Simple conversion
            Convert.convert(adapter, origin, target);
        } else {
            if (origin instanceof TypeFilled originFld &&
                    target instanceof TypeFilled targetFld) {
                if (originFld.isPrimitive() && targetFld.isPrimitive()) {
                    //Primitive conversion
                    adapter.cast(TypeUtil.asmType(origin), TypeUtil.asmType(target));
                } else if (Types.unbox(originFld).isPrimitive() || Types.unbox(targetFld).isPrimitive()) {

                    if (origin.isPrimitive() && !target.isPrimitive()) {
                        //Primitive -> Box conversion
                        TypeFilled primTarget = Types.unbox(targetFld);
                        if (primTarget.isPrimitive()) {

                        } else {
                            Convert.primitiveBoxing(adapter, origin);
                            adapter.cast(TypeUtil.asmType(Types.box(originFld)), TypeUtil.asmType(target));
                        }

                    }

                }

            }
        }
    }

    @Override
    public TypeInformal type() {
        return this.targetType;
    }
}
