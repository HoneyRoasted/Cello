package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;

public interface Environment {

    Verification<ClassNode> lookup(Namespace namespace);

    default Verification<ClassNode> lookup(TypeParameterized type) {
        return lookup(type.namespace());
    }

    default Verification<ClassNode> lookup(TypeFilled type) {
        return lookup(type.type().namespace());
    }

    default Verification<ClassNode> lookup(Class<?> cls) {
        return lookup(Namespace.of(cls));
    }

    default <T extends Type> Verification<T> resolve(T type) {
        if (type instanceof TypeInformal inf) {
            if (inf instanceof TypeFilled fld) {
                Verification<TypeParameterized> opt = resolve(fld.type());
                if (opt.success() && opt.value().isPresent()) {
                    inf = fld.toBuilder().type(opt.value().get()).build();
                } else {
                    return (Verification<T>) opt;
                }
            }

            boolean[] success = {true};
            TypeInformal res = inf.map(t -> {
                Verification<TypeInformal> opt = resolve(t);
                if (opt.success() && opt.value().isPresent()) {
                    return opt.value().get();
                } else {
                    success[0] = false;
                    return t;
                }
            });

            if (success[0]) {
                return Verification.success((T) res);
            }
        } else if (type instanceof TypeParameterized prm) {
            return (Verification<T>) lookup(prm.namespace()).map(ClassNode::parameterizedType);
        }

        return Verification.<T>builder().
                error(Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not resolve type %s", type.internalName())
                .build();
    }

}
