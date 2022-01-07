package honeyroasted.cello.node.structure;

import honeyroasted.javatype.parameterized.TypeVar;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ParameterizedNode {

    Optional<TypeVar> fetchTypeVar(String name);

    void putTypeVar(String name, TypeVar var);

    TypeVar defineTypeVar(String name);

    List<TypeVar> definedTypeVars();

    Map<String, TypeVar> typeVars();

    default void setTypeVars(List<TypeVar> definedTypes) {
        this.typeVars().clear();
        this.definedTypeVars().clear();
        this.definedTypeVars().addAll(definedTypes);
        definedTypes.forEach(t -> putTypeVar(t.name(), t));
    }

    default TypeVar fetchOrPutTypeVar(String name) {
        return fetchTypeVar(name).orElseGet(() -> {
            TypeVar v = new TypeVar();
            putTypeVar(name, v);
            return v;
        });
    }

    default boolean hasTypeVar(String name) {
        return fetchTypeVar(name).isPresent();
    }
}
