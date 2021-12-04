package honeyroasted.cello.node.ast.instruction.value;

import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.informal.TypeInformal;

public class Coerce extends AbstractPropertyHolder implements TypedNode<Coerce, Coerce> {
    private TypedNode<?, ?> value;
    private TypeInformal target;

    public Coerce(TypedNode<?, ?> value, TypeInformal target) {
        this.value = value;
        this.target = target;
    }

    @Override
    public TypeInformal type() {
        return this.target;
    }

}
