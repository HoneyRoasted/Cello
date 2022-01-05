package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationException;
import honeyroasted.javatype.parameterized.TypeVar;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeVarScope {
    private Map<String, TypeVar> vars = new LinkedHashMap<>();
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
            throw new VerificationException(Verification.builder()
                    .typeVarNotFoundError(name)
                    .build());
        } else {
            TypeVar var = new TypeVar();
            this.vars.put(name, var);
            return var;
        }
    }
}
