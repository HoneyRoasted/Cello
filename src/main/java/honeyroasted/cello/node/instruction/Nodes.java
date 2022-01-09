package honeyroasted.cello.node.instruction;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.val.Constant;
import honeyroasted.cello.node.instruction.val.Conversion;
import honeyroasted.cello.node.instruction.val.LoadThis;
import honeyroasted.cello.node.instruction.val.PrimitiveConstant;
import honeyroasted.cello.node.instruction.val.TypeConstant;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.function.BiFunction;

public interface Nodes {

    static Constant<?> defaultValue(TypeFilled type) {
        if (type.equals(Types.BOOLEAN)) {
            return constant(false);
        } else if (type.equals(Types.BYTE)) {
            return constant((byte) 0);
        } else if (type.equals(Types.CHAR)) {
            return constant('\0');
        } else if (type.equals(Types.SHORT)) {
            return constant((short) 0);
        } else if (type.equals(Types.INT)) {
            return constant(0);
        } else if (type.equals(Types.LONG)) {
            return constant(0L);
        } else if (type.equals(Types.FLOAT)) {
            return constant(0F);
        } else if (type.equals(Types.DOUBLE)) {
            return constant(0D);
        } else {
            return constant(null);
        }
    }

    static Constant<?> constant(Object val) {
        if (val instanceof Class<?> cls) {
            return new TypeConstant(Types.type(cls));
        } else if (val instanceof Type typ) {
            return new TypeConstant(typ);
        } else {
            return new PrimitiveConstant(val);
        }
    }

    static Node conversion(Node node, BiFunction<Environment, CodeContext, TypeInformal> type) {
        return new Conversion(node, type.andThen(Verification::success));
    }
    
    static Node conversion(Node node, TypeInformal type) {
        return conversion(node, (e, c) -> type);
    }

    static Node unwrapConversion(Node node) {
        return node instanceof Conversion cnv ? cnv.value() : node;
    }

}
