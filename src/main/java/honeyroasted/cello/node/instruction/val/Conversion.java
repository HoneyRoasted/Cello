package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public class Conversion extends AbstractNode implements Node {
    @Child
    private Node value;
    private BiFunction<Environment, CodeContext, TypeInformal> target;

    public Conversion(Node value, BiFunction<Environment, CodeContext, TypeInformal> target) {
        this.value = value;
        this.target = target;
    }

    @Override
    protected void doExpected(Environment environment, CodeContext context) {
        this.value.setExpected(this.target.apply(environment, context));
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeInformal target = this.target.apply(environment, context);
        if (target == null || this.value.type().isAssignableTo(target)) {
            return Verification.success(this, target);
        } else {
            return Verification.error(Verify.Code.TYPE_ERROR, "%s is not assignable to %s", this.value.type().externalName(), target.externalName());
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);

        TypeInformal src = this.value.type();
        TypeInformal dst = this.target.apply(environment, context);

        if (dst != null) {
            if (src instanceof TypeFilled srcFld && dst instanceof TypeFilled dstFld) {
                if (srcFld.isPrimitive() && dstFld.isPrimitive()) {
                    //Primitive conversion
                    adapter.cast(TypeUtil.asmType(srcFld), TypeUtil.asmType(dstFld));
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
                }
            }
        }
    }
}
