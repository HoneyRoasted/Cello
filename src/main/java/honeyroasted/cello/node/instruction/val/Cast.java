package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public class Cast extends AbstractNode implements Node, BooleanValue {
    @Child
    private Node value;
    private BiFunction<Environment, CodeContext, Verification<TypeInformal>> target;

    public Cast(Node value, BiFunction<Environment, CodeContext, Verification<TypeInformal>> target) {
        this.value = value;
        this.target = target;
    }

    public Node value() {
        return this.value;
    }

    @Override
    protected void doExpected(Environment environment, CodeContext context) {
        this.target.apply(environment, context).value().ifPresent(t -> this.value.setExpected(t));
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        Verification<TypeInformal> verification = this.target.apply(environment, context);
        if (verification.success()) {
            TypeInformal target = verification.value().orElse(null);

            if (target == null || this.value.type().isAssignableTo(target) || target.isAssignableTo(this.value.type())) {
                return Verification.success(this, target);
            } else {
                return Verification.error(this, Verify.Code.TYPE_ERROR, "%s is not castable to %s", this.value.type(), target);
            }
        } else {
            return verification;
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);

        TypeInformal src = this.value.type();
        TypeInformal dst = this.target.apply(environment, context).value().orElse(null);

        cast(src, dst, adapter);
    }

    public static void cast(TypeInformal src, TypeInformal dst, InstructionAdapter adapter) {
        if (dst != null) {
            if (src instanceof TypeFilled srcFld && dst instanceof TypeFilled dstFld) {
                if (srcFld.isAssignableTo(dstFld)) {
                    Convert.convert(src, dst, adapter);
                } else if (srcFld.isPrimitive() && dstFld.isPrimitive()) {
                    //Primitive conversion
                    adapter.cast(TypeUtil.asmType(srcFld), TypeUtil.asmType(dstFld));
                } else if (!srcFld.isPrimitive() && Types.unbox(srcFld).isPrimitive() &&
                        !dstFld.isPrimitive() && Types.unbox(dstFld).isPrimitive()) {
                    //Conversion between primitive boxes
                    cast(srcFld, Types.unbox(srcFld), adapter);
                    cast(Types.unbox(srcFld), dst, adapter);
                } else if (srcFld.isPrimitive() && !dstFld.isPrimitive()) {
                    //Primitive boxing

                    if (Types.unbox(dstFld).isPrimitive()) {
                        adapter.cast(TypeUtil.asmType(srcFld), TypeUtil.asmType(Types.unbox(dstFld)));
                        adapter.invokestatic(dstFld.internalName(), "valueOf",
                                Types.method().returnType(dstFld).addParameter(Types.unbox(dstFld)).build().descriptor(), false);
                    } else {
                        adapter.invokestatic(Types.box(srcFld).internalName(), "valueOf",
                                Types.method().returnType(Types.box(srcFld)).addParameter(srcFld).build().descriptor(), false);

                        if (!Types.box(srcFld).isAssignableTo(dstFld)) {
                            adapter.checkcast(TypeUtil.asmType(dstFld));
                        }
                    }

                } else if (!srcFld.isPrimitive() && dstFld.isPrimitive()) {
                    //Primitive unboxing

                    if (Types.unbox(srcFld).isPrimitive()) {
                        adapter.invokevirtual(Types.unbox(srcFld).internalName(), Types.unbox(srcFld).externalName() + "Value",
                                Types.method().returnType(Types.unbox(srcFld)).build().descriptor(), false);
                        adapter.cast(TypeUtil.asmType(Types.unbox(srcFld)), TypeUtil.asmType(dstFld));
                    } else {
                        if (!srcFld.isAssignableTo(Types.box(dstFld))) {
                            adapter.checkcast(TypeUtil.asmType(Types.box(dstFld)));
                        }

                        adapter.invokevirtual(Types.box(dstFld).internalName(), dstFld.externalName() + "Value",
                                Types.method().returnType(dstFld).build().descriptor(), false);
                    }
                } else if (!src.erasure().isAssignableTo(dst.erasure())) {
                    adapter.checkcast(TypeUtil.asmType(dst));
                }
            } else if (!src.erasure().isAssignableTo(dst.erasure())) {
                adapter.checkcast(TypeUtil.asmType(dst));
            }
        }
    }

    @Override
    public void jumpIfTrue(Label ifTrue, InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.value instanceof BooleanValue bop && this.value.type().isAssignableTo(Types.BOOLEAN)) {
            bop.jumpIfTrue(ifTrue, adapter, environment, context);
        } else {
            this.apply(adapter, environment, context);
            adapter.ifne(ifTrue);
        }
    }

    @Override
    public void jumpIfFalse(Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.value instanceof BooleanValue bop && this.value.type().isAssignableTo(Types.BOOLEAN)) {
            bop.jumpIfFalse(ifFalse, adapter, environment, context);
        } else {
            this.apply(adapter, environment, context);
            adapter.ifeq(ifFalse);
        }
    }

    @Override
    public void jump(Label ifTrue, Label ifFalse, InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (this.value instanceof BooleanValue bop && this.value.type().isAssignableTo(Types.BOOLEAN)) {
            bop.jump(ifTrue, ifFalse, adapter, environment, context);
        } else {
            this.apply(adapter, environment, context);
            adapter.ifne(ifTrue);
            adapter.goTo(ifFalse);
        }
    }

}
