package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;

public class ParameterNode extends AbstractAnnotated {
    private TypeInformal type;
    private String name;

    public ParameterNode(TypeInformal type, String name) {
        this.type = type;
        this.name = name;
    }

    public TypeInformal type() {
        return this.type;
    }

    public String name() {
        return this.name;
    }
}
