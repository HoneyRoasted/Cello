package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.parameterized.TypeVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractParameterized extends AbstractAnnotated implements ParameterizedNode {
    private List<TypeVar> typeVars = new ArrayList<>();
    private Map<String, TypeVar> typeVarMap = new HashMap<>();

    protected Supplier<ParameterizedNode> parent;

    @Override
    public Optional<TypeVar> fetchTypeVar(String name) {
        TypeVar var = this.typeVarMap.get(name);
        if (var == null) {
            ParameterizedNode parent = this.parent.get();
            if (parent != null) {
                return parent.fetchTypeVar(name);
            }
        }

        return Optional.ofNullable(var);
    }

    @Override
    public void putTypeVar(String name, TypeVar var) {
        this.typeVarMap.put(name, var);
    }

    @Override
    public TypeVar defineTypeVar(String name) {
        TypeVar var = this.typeVarMap.get(name);
        if (var != null) {
            if (!this.typeVars.contains(var)) {
                this.typeVars.add(var);
            }
            return var;
        } else {
            var = new TypeVar();
            this.putTypeVar(name, var);
            if (!this.typeVars.contains(var)) {
                this.typeVars.add(var);
            }
            return var;
        }
    }

    @Override
    public List<TypeVar> definedTypeVars() {
        return this.typeVars;
    }

    @Override
    public Map<String, TypeVar> typeVars() {
        return this.typeVarMap;
    }

}
