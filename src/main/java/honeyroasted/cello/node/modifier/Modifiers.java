package honeyroasted.cello.node.modifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Modifiers {
    private Set<Modifier> modifiers = new HashSet<>();

    public Modifiers add(Modifier... modifiers) {
        Collections.addAll(this.modifiers, modifiers);
        return this;
    }

    public Modifiers remove(Modifier... modifiers) {
        this.modifiers.removeAll(Set.of(modifiers));
        return this;
    }

    public int toBits() {
        int mods = 0;
        for (Modifier mod : this.modifiers) {
            mods |= mod.code();
        }
        return mods;
    }

    public int toBits(ModifierTarget target) {
        int mods = 0;
        for (Modifier mod : this.modifiers) {
            if (mod.targets(target)) {
                mods |= mod.code();
            }
        }
        return mods;
    }

}
