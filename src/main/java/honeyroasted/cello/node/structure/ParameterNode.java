package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.informal.TypeFilled;

public class ParameterNode extends AbstractAnnotated {
    private TypeFilled type;
    private String name;

    public ParameterNode(TypeFilled type, String name) {
        this.type = type;
        this.name = name;
    }

    public TypeFilled type() {
        return this.type;
    }

    public String name() {
        return this.name;
    }
}
