package honeyroasted.cello.node.modifier;

public enum Access {
    PRIVATE (Modifier.PRIVATE),
    PROTECTED (Modifier.PROTECTED),
    PACKAGE_PROTECTED (null),
    PUBLIC (Modifier.PUBLIC);

    private static final Access[][] accessMap = {
            {PRIVATE, PROTECTED, PACKAGE_PROTECTED, PUBLIC},
            {PACKAGE_PROTECTED, PROTECTED, PUBLIC},
            {PROTECTED, PUBLIC},
            {PUBLIC}
    };

    private Modifier modifier;

    Access(Modifier modifier) {
        this.modifier = modifier;
    }

    public Modifier modifier() {
        return this.modifier;
    }

    public boolean canAccess(Access other) {
        for (Access[] map : accessMap) {
            if (map[0] == this) {
                for (Access a : map) {
                    if (a == other) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int code() {
        return this.modifier != null ? this.modifier.code() : 0;
    }

    public void apply(Modifiers modifiers) {
        if (this.modifier != null) {
            modifiers.add(this.modifier);
        }
    }

}
