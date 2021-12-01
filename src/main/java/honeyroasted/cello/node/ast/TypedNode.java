package honeyroasted.cello.node.ast;

import honeyroasted.javatype.informal.TypeInformal;

public interface TypedNode<T extends TypedNode> extends CodeNode<T> {

    TypeInformal type();

    default void provideExpected(TypeInformal type) {

    }

}
