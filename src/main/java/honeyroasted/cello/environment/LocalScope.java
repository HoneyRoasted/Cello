package honeyroasted.cello.environment;

import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LocalScope {
    private LocalScope parent;
    private Map<String, Var> vars;
    private int counter;

    public LocalScope() {
        this(null);
    }

    public LocalScope(LocalScope parent) {
        this(parent, new LinkedHashMap<>(), 0);
    }

    private LocalScope(LocalScope parent, Map<String, Var> vars, int counter) {
        this.parent = parent;
        this.vars = vars;
        this.counter = counter;
    }

    public LocalScope child() {
        return new LocalScope(this);
    }

    public LocalScope copy() {
        Map<String, Var> varsCopy = new LinkedHashMap<>(this.vars);
        varsCopy.replaceAll((k, v) -> v.copy());
        return new LocalScope(this.parent == null ? null : this.parent.copy(), varsCopy, this.counter);
    }

    private int defineIndex(TypeInformal type) {
        if (this.parent != null) {
            return this.parent.defineIndex(type);
        } else {
            int res = this.counter;
            this.counter += TypeUtil.size(type);
            return res;
        }
    }

    public Optional<Var> define(String name, TypeInformal type) {
        if (has(name) || type.equals(Types.VOID)) {
            return Optional.empty();
        } else {
            Var v = new Var(type, defineIndex(type));
            this.vars.put(name, v);
            return Optional.of(v);
        }
    }

    public boolean has(String name) {
        return fetch(name).isPresent();
    }

    public Optional<Var> fetch(String name) {
        Var v = this.vars.get(name);
        if (v == null) {
            return this.parent != null ? this.parent.fetch(name) :
                    Optional.empty();
        } else {
            return Optional.of(v);
        }
    }

}
