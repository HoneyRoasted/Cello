package honeyroasted.cello.node;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.instruction.flow.ScopeBlock;
import honeyroasted.cello.node.instruction.flow.SequenceBlock;
import honeyroasted.cello.node.instruction.value.Constant;
import honeyroasted.cello.node.instruction.value.Convert;
import honeyroasted.cello.node.instruction.value.TypeConstant;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.Arrays;
import java.util.function.BiFunction;

public interface Nodes {

    static TypedNode<?, ?> defaultValue(TypeFilled type) {
        if (type.equals(Types.BOOLEAN)) {
            return Nodes.constant(false);
        } else if (type.equals(Types.BYTE)) {
            return Nodes.constant((byte) 0);
        } else if (type.equals(Types.CHAR)) {
            return Nodes.constant('\0');
        } else if (type.equals(Types.SHORT)) {
            return Nodes.constant((short) 0);
        } else if (type.equals(Types.INT)) {
            return Nodes.constant(0);
        } else if (type.equals(Types.LONG)) {
            return Nodes.constant(0L);
        } else if (type.equals(Types.FLOAT)) {
            return Nodes.constant(0F);
        } else if (type.equals(Types.DOUBLE)) {
            return Nodes.constant(0D);
        } else {
            return Nodes.constant(null);
        }
    }

    static <K extends TypedNode<?, ?> & AnnotationValue> K constant(Object val) {
        if (val instanceof TypeInformal type) {
            return (K) new TypeConstant(type);
        } else {
            return (K) new Constant(val);
        }
    }

    static TypedNode<?, ?> convert(TypedNode<?, ?> val, TypeInformal type) {
        return new Convert(val, (x, y) -> type);
    }

    static TypedNode<?, ?> convert(TypedNode<?, ?> val, BiFunction<Environment, CodeContext, TypeInformal> target) {
        return new Convert(val, target);
    }

    static ScopeBlock scope(CodeNode<?, ?> node) {
        return new ScopeBlock(node);
    }

    static SequenceBlock sequence(CodeNode<?, ?>... nodes) {
        return new SequenceBlock(Arrays.asList(nodes));
    }

}
