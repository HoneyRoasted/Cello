package honeyroasted.cello.environment.control;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ControlScope {
    private ControlScope parent;
    private String name;
    private Map<Kind, Control> controls = new LinkedHashMap<>();

    public ControlScope(ControlScope parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public static ControlScope create() {
        return new ControlScope(null, null);
    }

    public static ControlScope create(String name) {
        return new ControlScope(null, null);
    }

    public ControlScope child() {
        return new ControlScope(this, null);
    }

    public ControlScope child(String name) {
        return new ControlScope(this, name);
    }

    public ControlScope guarantee(Kind... kinds) {
        for (Kind k : kinds) {
            get(k);
        }
        return this;
    }

    public ControlScope guaranteeAll() {
        return guarantee(Kind.values());
    }

    public boolean has(Kind kind) {
        return this.controls.containsKey(kind);
    }

    public boolean has(Kind kind, String name) {
        if (name == null) {
            return has(kind);
        } else {
            return fetch(name).map(c -> c.has(kind)).orElse(false);
        }
    }

    public Control get(Kind kind) {
        return this.controls.computeIfAbsent(kind, k -> SimpleControl.optional());
    }

    public Control require(Kind kind) {
        return this.controls.computeIfAbsent(kind, k -> SimpleControl.present());
    }

    public Control getRef(Kind kind, Kind ref) {
        return this.controls.computeIfAbsent(kind, k -> new ReferenceControl(get(ref)));
    }

    public Control requireRef(Kind kind, Kind ref) {
        return this.controls.computeIfAbsent(kind, k -> new ReferenceControl(require(ref)));
    }

    public Optional<ControlScope> fetch(String name) {
        if (name == null) {
            return Optional.of(this);
        }

        if (Objects.equals(name, this.name)) {
            return Optional.of(this);
        } else if (this.parent != null) {
            return this.parent.fetch(name);
        }

        return Optional.empty();
    }

    public boolean has(String name) {
        return fetch(name).isPresent();
    }

    public enum Kind {
        START,
        END,
        CONDITION;
    }

}
