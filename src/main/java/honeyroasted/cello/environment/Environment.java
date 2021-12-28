package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.Optional;

public interface Environment {

    Optional<ClassNode> lookup(Namespace namespace);

    default Optional<ClassNode> lookup(TypeParameterized type) {
        return lookup(type.namespace());
    }

    default Optional<ClassNode> lookup(TypeFilled type) {
        return lookup(type.type().namespace());
    }

    default Optional<ClassNode> lookup(Class<?> cls) {
        return lookup(Namespace.of(cls));
    }

}
