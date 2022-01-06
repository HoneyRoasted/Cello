package honeyroasted.cello.environment;

import honeyroasted.javatype.parameterized.TypeVar;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeVarScope {
    private Map<String, TypeVar> vars = new LinkedHashMap<>();
    private Set<TypeVar> defined = new HashSet<>();

    private TypeVarScope parent;

    private Set<TypeVarScope> children = new HashSet<>();

    public TypeVarScope(TypeVarScope parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public TypeVarScope() {
        this(null);
    }

    public void provideParent(TypeVarScope parent) {
        if (this.parent == null) {
            this.parent = parent;
            this.parent.children.add(this);
        }
    }

    public Optional<TypeVar> fetch(String name) {
        return vars.containsKey(name) ? Optional.of(vars.get(name)) :
                this.parent != null ? this.parent.fetch(name) : Optional.empty();
    }

    public TypeVar fetchOrPut(String name) {
        Optional<TypeVar> var = fetch(name);
        if (var.isPresent()) {
            return var.get();
        } else {
            TypeVar v = new TypeVar();
            put(name, v);
            return v;
        }
    }

    public boolean has(String name) {
        return fetch(name).isPresent();
    }

    public void put(String name, TypeVar var) {
        this.vars.put(name, var);
    }

    public TypeVarScope child() {
        return new TypeVarScope(this);
    }

    public TypeVar define(String name) {
        if (this.vars.containsKey(name)) {
            TypeVar var = this.vars.get(name);
            this.defined.add(var);
            return var;
        } else {
            TypeVar var = new TypeVar();
            this.vars.put(name, var);
            this.defined.add(var);
            return var;
        }
    }

    public Set<TypeVar> defined() {
        return this.defined;
    }

    public Map<String, TypeVar> vars() {
        return this.vars;
    }
}
