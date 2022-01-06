package honeyroasted.cello.environment;

import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.method.TypeMethodFilled;
import honeyroasted.javatype.method.TypeMethodParameterized;

import java.util.HashSet;
import java.util.Set;

public interface TypeUtil {

    static int size(TypeInformal informal) {
        if (informal.equals(Types.VOID)) {
            return 0;
        } else if (informal.equals(Types.DOUBLE) || informal.equals(Types.LONG)) {
            return 2;
        } else {
            return 1;
        }
    }

    static org.objectweb.asm.Type asmType(Type type) {
        if (type instanceof TypeFilled ||
                type instanceof TypeArray ||
                type instanceof TypeMethodFilled ||
                type instanceof TypeMethodParameterized) {
            return org.objectweb.asm.Type.getType(type.descriptor());
        } else {
            return org.objectweb.asm.Type.getType(Object.class);
        }
    }

    static Set<TypeClass> flatten(TypeInformal type) {
        Set<TypeClass> set = new HashSet<>();
        if (type instanceof TypeClass cls) {
            set.add(cls);
        } else {
            type.pseudoParents().forEach(t -> set.addAll(flatten(t)));
        }
        return set;
    }

}
