package honeyroasted.cello.environment.control;

import org.objectweb.asm.Label;

public class SimpleControl implements Control {
    private Label label;

    public static SimpleControl optional() {
        return new SimpleControl();
    }

    public static SimpleControl present() {
        SimpleControl c = new SimpleControl();
        c.label();
        return c;
    }

    public boolean hasLabel() {
        return this.label != null;
    }

    public Label label() {
        if (this.label == null) {
            this.label = new Label();
        }
        return this.label;
    }

}
