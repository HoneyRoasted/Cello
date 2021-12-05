package honeyroasted.cello.node;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.ast.instruction.flow.ScopeBlock;
import honeyroasted.cello.node.ast.instruction.flow.SequenceBlock;
import honeyroasted.cello.node.ast.instruction.value.Constant;
import honeyroasted.cello.node.ast.instruction.value.Convert;
import honeyroasted.cello.node.ast.instruction.value.TypeConstant;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.Arrays;
import java.util.function.BiFunction;

public interface Nodes {

    static TypedNode<?, ?> constant(Object val) {
        if (val instanceof TypeInformal type) {
            return new TypeConstant(type);
        } else {
            return new Constant(val);
        }
    }

    static TypedNode<?, ?> convert(TypedNode<?, ?> val, TypeInformal type) {
        return new Convert(val, (x, y) -> type);
    }

    static TypedNode<?, ?> convert(TypedNode<?, ?> val, BiFunction<Environment, LocalScope, TypeInformal> target) {
        return new Convert(val, target);
    }

    static ScopeBlock scope(CodeNode<?, ?> node) {
        return new ScopeBlock(node);
    }

    static SequenceBlock sequence(CodeNode<?, ?>... nodes) {
        return new SequenceBlock(Arrays.asList(nodes));
    }

}
