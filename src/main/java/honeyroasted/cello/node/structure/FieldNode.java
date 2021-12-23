package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.javatype.informal.TypeFilled;

public class FieldNode {
    private TypeFilled type;
    private TypedNode value;

    public FieldNode(TypeFilled type, TypedNode value) {
        this.type = type;
        this.value = value;
    }

    public TypeFilled type() {
        return this.type;
    }

    public TypedNode value() {
        return this.value;
    }
}
