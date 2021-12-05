package honeyroasted.cello.environment.control;

import org.objectweb.asm.Label;

public class ReferenceControl implements Control {
    private Control ref;

    public ReferenceControl(Control ref) {
        this.ref = ref;
    }

    @Override
    public boolean hasLabel() {
        return this.ref.hasLabel();
    }

    @Override
    public Label label() {
        return this.ref.label();
    }
}
