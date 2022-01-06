package honeyroasted.cello.environment.context;

import org.objectweb.asm.Label;

public class Control {
    private String name;
    private Label label;

    public Control(String name, Label label) {
        this.name = name;
        this.label = label;
    }

    public String name() {
        return this.name;
    }

    public Label label() {
        if (this.label == null) {
            this.label = new Label();
        }
        return this.label;
    }

    public Control copy() {
        return new Control(this.name, this.label);
    }

    public Label rawLabel() {
        return this.label;
    }

    public boolean hasLabel() {
        return this.label != null;
    }

    public enum Kind {
        BREAK,
        CONTINUE
    }
}
