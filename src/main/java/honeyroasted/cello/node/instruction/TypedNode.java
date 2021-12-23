package honeyroasted.cello.node.instruction;

import honeyroasted.javatype.informal.TypeInformal;

public interface TypedNode<T extends TypedNode, K extends TypedNode> extends CodeNode<T, K> {

    TypeInformal type();

    @Override
    default K preprocess() {
        return CodeNode.super.preprocess();
    }

    @Override
    default K preprocessFully() {
        return CodeNode.super.preprocessFully();
    }

    default void provideExpected(TypeInformal type) {

    }

}
