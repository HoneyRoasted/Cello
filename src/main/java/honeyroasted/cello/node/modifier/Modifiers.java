package honeyroasted.cello.node.modifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Modifiers {
    private Set<Modifier> modifiers = new HashSet<>();

    public boolean has(Modifier... modifiers) {
        for (Modifier mod : modifiers) {
            if (!this.modifiers.contains(mod)) {
                return false;
            }
        }
        return true;
    }

    public Access access() {
        for (Access access : Access.values()) {
            if (access.modifier() != null && has(access.modifier())) {
                return access;
            }
        }

        return Access.PACKAGE_PROTECTED;
    }

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

    public static Modifiers fromBits(int mods) {
        Modifiers modifiers = new Modifiers();
        for (Modifier mod : Modifier.values()) {
            if ((mod.code() & mods) != 0) {
                modifiers.add(mod);
            }
        }
        return modifiers;
    }

    public static Modifiers fromBits(int mods, ModifierTarget target) {
        Modifiers modifiers = new Modifiers();
        for (Modifier mod : Modifier.values()) {
            if (mod.targets(target) && (mod.code() & mods) != 0) {
                modifiers.add(mod);
            }
        }
        return modifiers;
    }

    public void set(Modifiers modifiers) {
        this.modifiers.clear();
        this.modifiers.addAll(modifiers.modifiers);
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }
}
