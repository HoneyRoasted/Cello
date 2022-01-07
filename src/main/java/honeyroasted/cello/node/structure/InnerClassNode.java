package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.modifier.AbstractModifiable;

public class InnerClassNode extends AbstractModifiable {
    private ClassNode cls;
    private String innerName;
    private boolean anonymous;

    public InnerClassNode(ClassNode cls, String innerName, boolean anonymous) {
        this.cls = cls;
        this.innerName = innerName;
        this.anonymous = anonymous;
    }

    public boolean isAnonymous() {
        return this.anonymous;
    }

    public String innerName() {
        return this.innerName;
    }

    public ClassNode classNode() {
        return this.cls;
    }

    public InnerClassNode setClassNode(ClassNode cls) {
        this.cls = cls;
        return this;
    }
}
