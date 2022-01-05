package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
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

    default <T extends Type> Optional<T> resolve(T type) {
        if (type instanceof TypeInformal inf) {
            if (inf instanceof TypeFilled fld) {
                Optional<TypeParameterized> opt = resolve(fld.type());
                if (opt.isPresent()) {
                    inf = fld.toBuilder().type(opt.get()).build();
                } else {
                    return Optional.empty();
                }
            }

            boolean[] success = {true};
            TypeInformal res = inf.map(t -> {
                Optional<TypeInformal> opt = resolve(t);
                if (opt.isPresent()) {
                    return opt.get();
                } else {
                    success[0] = false;
                    return t;
                }
            });

            if (success[0]) {
                return Optional.of((T) res);
            }
        } else if (type instanceof TypeParameterized prm) {
            return (Optional<T>) lookup(prm.namespace()).map(ClassNode::type);
        }

        return Optional.empty();
    }

}
