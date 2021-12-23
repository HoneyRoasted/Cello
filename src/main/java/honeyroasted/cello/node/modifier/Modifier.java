package honeyroasted.cello.node.modifier;

import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public enum Modifier {
    PUBLIC(ACC_PUBLIC, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD),
    PRIVATE(ACC_PRIVATE, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD),
    PROTECTED(ACC_PROTECTED, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD),
    STATIC(ACC_STATIC, ModifierTarget.FIELD, ModifierTarget.METHOD),
    FINAL(ACC_FINAL, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD, ModifierTarget.PARAMETER),
    SUPER(ACC_SUPER, ModifierTarget.CLASS),
    SYNCHRONIZED(ACC_SYNCHRONIZED, ModifierTarget.METHOD),
    OPEN(ACC_OPEN, ModifierTarget.MODULE),
    TRANSITIVE(ACC_TRANSITIVE, ModifierTarget.MODULE),
    VOLATILE(ACC_VOLATILE, ModifierTarget.FIELD),
    BRIDGE(ACC_BRIDGE, ModifierTarget.METHOD),
    STATIC_PHASE(ACC_STATIC_PHASE, ModifierTarget.MODULE),
    VARARGS(ACC_VARARGS, ModifierTarget.METHOD),
    TRANSIENT(ACC_TRANSITIVE, ModifierTarget.FIELD),
    NATIVE(ACC_NATIVE, ModifierTarget.METHOD),
    INTERFACE(ACC_INTERFACE, ModifierTarget.CLASS),
    ABSTRACT(ACC_ABSTRACT, ModifierTarget.CLASS, ModifierTarget.METHOD),
    STRICT(ACC_STRICT, ModifierTarget.METHOD),
    SYNTHETIC(ACC_SYNTHETIC, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD, ModifierTarget.PARAMETER, ModifierTarget.MODULE),
    ANNOTATION(ACC_ANNOTATION, ModifierTarget.CLASS),
    ENUM(ACC_ENUM, ModifierTarget.CLASS, ModifierTarget.FIELD),
    MANDATED(ACC_MANDATED, ModifierTarget.FIELD, ModifierTarget.METHOD, ModifierTarget.PARAMETER, ModifierTarget.MODULE),
    MODULE(ACC_MODULE, ModifierTarget.CLASS),
    RECORD(ACC_RECORD, ModifierTarget.CLASS),
    DEPRECATED(ACC_DEPRECATED, ModifierTarget.CLASS, ModifierTarget.FIELD, ModifierTarget.METHOD);

    private final int code;
    private final Set<ModifierTarget> targets;

    Modifier(int code, ModifierTarget... targets) {
        this.code = code;
        this.targets = Set.of(targets);
    }

    public int code() {
        return this.code;
    }

    public Set<ModifierTarget> targets() {
        return this.targets;
    }

    public boolean targets(ModifierTarget target) {
        return targets().contains(target);
    }
}
