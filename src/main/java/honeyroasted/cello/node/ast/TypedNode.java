package honeyroasted.cello.node.ast;

import honeyroasted.javatype.informal.TypeInformal;

public interface TypedNode extends CodeNode {

    TypeInformal type();

    default void provideExpected(TypeInformal type) {

    }

}
