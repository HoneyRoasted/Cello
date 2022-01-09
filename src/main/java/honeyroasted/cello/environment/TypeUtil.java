package honeyroasted.cello.environment;

import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.informal.TypeWild;
import honeyroasted.javatype.method.TypeMethodFilled;
import honeyroasted.javatype.method.TypeMethodParameterized;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

    static org.objectweb.asm.Type asmType(Namespace namespace) {
        return asmType(TypeParameterized.builder().namespace(namespace).build());
    }

    static org.objectweb.asm.Type asmType(Type type) {
        return org.objectweb.asm.Type.getType(type instanceof TypeInformal inf ?
                inf.erasure().descriptor() : type.descriptor());
    }

    static TypeInformal widestPrimitive(TypeInformal a, TypeInformal b) {
        if (a.equals(Types.DOUBLE) || b.equals(Types.DOUBLE)) {
            return Types.DOUBLE;
        } else if (a.equals(Types.FLOAT) || b.equals(Types.FLOAT)) {
            return Types.FLOAT;
        } else if (a.equals(Types.LONG) || b.equals(Types.LONG)) {
            return Types.LONG;
        } else if (a.equals(Types.INT) || b.equals(Types.INT)) {
            return Types.INT;
        } else if (a.equals(Types.SHORT) || b.equals(Types.SHORT)) {
            return Types.SHORT;
        } else if (a.equals(Types.CHAR) || b.equals(Types.CHAR)) {
            return Types.CHAR;
        } else if (a.equals(Types.BYTE) || b.equals(Types.BYTE)) {
            return Types.BYTE;
        } else if (a.equals(Types.BOOLEAN) || b.equals(Types.BOOLEAN)) {
            return Types.BOOLEAN;
        } else {
            return Types.VOID;
        }
    }

    static boolean isInteger32(TypeInformal a) {
        return a.equals(Types.INT) || a.equals(Types.SHORT) ||
                a.equals(Types.CHAR) || a.equals(Types.BYTE);
    }

    static boolean isNumeric(TypeInformal a) {
        return a.equals(Types.DOUBLE) || a.equals(Types.FLOAT) || a.equals(Types.LONG) ||
                a.equals(Types.INT) || a.equals(Types.SHORT) || a.equals(Types.CHAR) ||
                a.equals(Types.BYTE);
    }

    static Set<TypeClass> flatten(TypeInformal type) {
        Set<TypeClass> set = new LinkedHashSet<>();
        if (type instanceof TypeFilled cls) {
            set.add(cls);
        } else if (type instanceof TypeArray arr) {
            flatten(arr.element()).forEach(e -> set.add((TypeClass) e.array(1)));
        } else {
            type.pseudoParents().forEach(t -> set.addAll(flatten(t)));
        }
        return set;
    }

    static Verification<TypeInformal> commonParent(Environment environment, List<TypeInformal> types) {
        VerificationBuilder<TypeInformal> builder = Verification.builder();

        Set<TypeInformal> parents = Types.commonParents(types);
        for (TypeInformal type : parents) {
            if (type instanceof TypeFilled fld) {
                Verification<ClassNode> node = environment.lookup(fld);
                builder.child(node);
                if (node.success() && node.value().isPresent() && !node.value().get().modifiers().has(Modifier.INTERFACE)) {
                    builder.value(fld);
                    return builder.build();
                }
            }
        }

        return builder.value(Types.OBJECT).andChildren().build();
    }

}
