package honeyroasted.cello.node.ast.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public class Convert extends AbstractPropertyHolder implements TypedNode<Convert, Convert> {
    private TypedNode value;
    private BiFunction<Environment, LocalScope, TypeInformal> target;

    public Convert(TypedNode<?, ?> value, BiFunction<Environment, LocalScope, TypeInformal> target) {
        this.value = value;
        this.target = target;
    }

    private TypeInformal targetType;

    @Override
    public Convert preprocess() {
        this.value = this.value.preprocessFully();
        return this;
    }

    @Override
    public Verification<Convert> verify(Environment environment, LocalScope localScope) {
        TypeInformal target = this.target.apply(environment, localScope);
        this.targetType = target;
        this.value.provideExpected(target);

        Verification<TypedNode> verification = this.value.verify(environment, localScope);
        if (verification.success()) {
            if (!this.value.type().isAssignableTo(target)) {
                return Verification.builder(this)
                        .child(verification)
                        .typeError(this.value.type(), target)
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

        convert(adapter, origin, target);
    }

    public static void convert(InstructionAdapter adapter, TypeInformal origin, TypeInformal target) {
        if (origin.isPrimitive() || target.isPrimitive()) {
            if (origin.isPrimitive() && target.isPrimitive()) {
                //Primitive conversion
                adapter.cast(TypeUtil.asmType(origin), TypeUtil.asmType(target));
            } else if (origin.isPrimitive() && origin instanceof TypeFilled fld &&
                        target instanceof TypeFilled tFld) {
                //Primitive boxing
                TypeInformal unbox = Types.unbox(tFld);

                if (unbox.isPrimitive()) {
                    //Converting to a specific box type
                    adapter.cast(TypeUtil.asmType(fld), TypeUtil.asmType(unbox));
                    primitiveBoxing(adapter, unbox);
                } else {
                    //Converting to our own box type
                    primitiveBoxing(adapter, fld);
                }
            } else if (target.isPrimitive() && target instanceof TypeFilled fld &&
                        origin instanceof TypeFilled oFld) {
                //Primitive unboxing
                primitiveUnboxing(adapter, origin);
                adapter.cast(TypeUtil.asmType(Types.unbox(oFld)), TypeUtil.asmType(fld));
            }
        }
    }

    public static void primitiveBoxing(InstructionAdapter adapter, TypeInformal type) {
        if (type instanceof TypeFilled fld) {
            TypeInformal box = Types.box(fld);
            TypeInformal unbox = Types.unbox(fld);

            adapter.invokestatic(box.internalName(), "valueOf",
                    Types.method()
                            .returnType(box)
                            .addParameter(unbox)
                            .build().descriptor(), false);
        }
    }

    public static void primitiveUnboxing(InstructionAdapter adapter, TypeInformal type) {
        if (type instanceof TypeFilled fld) {
            TypeInformal box = Types.box(fld);
            TypeFilled unbox = Types.unbox(fld);

            adapter.invokevirtual(box.descriptor(), unbox.type().namespace().className() + "Value",
                    Types.method()
                            .returnType(unbox)
                            .build().descriptor(), false);
        }
    }

    @Override
    public TypeInformal type() {
        return this.targetType;
    }
}
