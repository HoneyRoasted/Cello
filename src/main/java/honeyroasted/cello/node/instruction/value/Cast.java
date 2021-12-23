package honeyroasted.cello.node.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeClass;
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
            if (target.isAssignableTo(this.value.type()) || this.value.type().isAssignableTo(target)) {
                return Verification.builder(this)
                        .child(verification)
                        .build();
            } else {
                return Verification.builder(this)
                        .illegalCastError(this.value.type(), target)
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
        } else if (origin instanceof TypeClass && target instanceof TypeClass){
            if (Types.unbox(origin).isPrimitive() || Types.unbox(target).isPrimitive()) {
                if (origin.isPrimitive() && target.isPrimitive()) {
                    //Primitive -> Primitive conversion
                    Convert.checkcast(adapter, origin, target);
                } else if (origin.isPrimitive() && !target.isPrimitive()) {
                    //Primitive -> Box conversion
                    TypeInformal primTarget = Types.unbox(target);
                    if (primTarget.isPrimitive()) {
                        Convert.checkcast(adapter, origin, primTarget);
                        Convert.primitiveBoxing(adapter, origin);
                    } else {
                        Convert.primitiveBoxing(adapter, origin);
                        Convert.checkcast(adapter, Types.box(origin), target);
                    }
                } else if (!origin.isPrimitive() && target.isPrimitive()) {
                    //Box -> Primitive conversion
                    TypeInformal primOrig = Types.unbox(origin);
                    if (primOrig.isPrimitive()) {
                        Convert.primitiveUnboxing(adapter, primOrig);
                        Convert.checkcast(adapter, primOrig, target);
                    } else {
                        Convert.checkcast(adapter, origin, Types.box(target));
                        Convert.primitiveUnboxing(adapter, target);
                    }
                } else {
                    //Box -> Box conversion
                    Convert.primitiveUnboxing(adapter, origin);
                    Convert.checkcast(adapter, Types.unbox(origin), Types.unbox(target));
                    Convert.primitiveBoxing(adapter, target);
                }
            } else {
                Convert.checkcast(adapter, origin, target);
            }
        }
    }

    @Override
    public TypeInformal type() {
        return this.targetType;
    }
}
