package honeyroasted.cello.environment;

import honeyroasted.javatype.informal.TypeInformal;

public class Var {
    private TypeInformal type;
    private int index;

    public Var(TypeInformal type, int index) {
        this.type = type;
        this.index = index;
    }

    public TypeInformal type() {
        return type;
    }

    public int index() {
        return index;
    }
}
