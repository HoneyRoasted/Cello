package honeyroasted.cello.node.instruction;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.control.Scope;
import honeyroasted.cello.node.instruction.control.Sequence;
import honeyroasted.cello.node.instruction.control.While;
import honeyroasted.cello.node.instruction.val.Cast;
import honeyroasted.cello.node.instruction.val.Constant;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.node.instruction.val.PrimitiveConstant;
import honeyroasted.cello.node.instruction.val.TypeConstant;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public interface Nodes {

    static Constant<?> defaultValue(TypeInformal type) {
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

    static Cast cast(Node node, BiFunction<Environment, CodeContext, TypeInformal> type) {
        return new Cast(node, type.andThen(Verification::success));
    }

    static Cast cast(Node node, TypeInformal type) {
        return cast(node, (e, c) -> type);
    }

    static Node unwrapCast(Node node) {
        return node instanceof Cast cnv ? cnv.value() : node;
    }

    static Convert convert(Node node, BiFunction<Environment, CodeContext, TypeInformal> type) {
        return new Convert(node, type.andThen(Verification::success));
    }
    
    static Convert convert(Node node, TypeInformal type) {
        return convert(node, (e, c) -> type);
    }

    static Node unwrapConvert(Node node) {
        return node instanceof Convert cnv ? cnv.value() : node;
    }

    static Node sequence(Node... nodes) {
        return new Sequence(Arrays.asList(nodes));
    }

    static Node sequence(List<Node> nodes) {
        return new Sequence(nodes);
    }

    static Node scope(Node node) {
        return new Scope(node);
    }

    static Node forLoop(Node init, Node cond, Node increment, Node body) {
        return sequence(init, new While(cond, sequence(body, increment)));
    }

}
