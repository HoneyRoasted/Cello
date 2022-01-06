package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.Optional;

public interface CachingEnvironment extends Environment {

    void cache(Namespace namespace, ClassNode node);

    default void cache(ClassNode node) {
        cache(node.type().namespace(), node);
    }

    void remove(ClassNode node);

    Optional<ClassNode> fromCache(Namespace namespace);

    default Optional<ClassNode> fromCache(TypeParameterized type) {
        return fromCache(type.namespace());
    }

    default Optional<ClassNode> fromCache(TypeFilled type) {
        return fromCache(type.type().namespace());
    }

    default Optional<ClassNode> fromCache(Class<?> cls) {
        return fromCache(Namespace.of(cls));
    }

}
