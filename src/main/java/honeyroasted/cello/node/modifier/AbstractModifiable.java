package honeyroasted.cello.node.modifier;

import honeyroasted.cello.properties.AbstractPropertyHolder;

public class AbstractModifiable extends AbstractPropertyHolder implements Modifiable {
    private Modifiers modifiers = new Modifiers();

    @Override
    public Modifiers modifiers() {
        return this.modifiers;
    }

}
