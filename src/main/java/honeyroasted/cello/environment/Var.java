package honeyroasted.cello.environment;

import honeyroasted.javatype.informal.TypeInformal;

public class Var {
    private TypeInformal type;
    private int index;

    private boolean initialized = false;

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

    public boolean initialized() {
        return this.initialized;
    }

    public Var setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this;
    }

    public Var copy() {
        Var var = new Var(this.type, this.index);
        var.setInitialized(this.initialized);
        return var;
    }
}
