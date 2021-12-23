package honeyroasted.cello.node.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class TypeConstant extends AbstractPropertyHolder implements TypedNode<TypeConstant, TypeConstant>, AnnotationValue {
    private TypeInformal type;

    public TypeConstant(TypeInformal type) {
        this.type = type;
    }

    public TypeConstant(TypeParameterized prm) {
        this(prm.withArguments());
    }

    @Override
    public Verification<TypeConstant> verify(Environment environment, LocalScope localScope) {
        if (this.type instanceof TypeFilled) {
            return Verification.success(this);
        } else if (this.type instanceof TypeArray arr) {
            TypeInformal element = arr.deepElement();
            if (element instanceof TypeFilled) {
                return Verification.success(this);
            }
        }

        return Verification.builder(this)
                .invalidTypeError(this.type)
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        if (this.type instanceof TypeFilled fld) {
            adapter.tconst(Type.getType(fld.type().descriptor()));
        } else if (this.type instanceof TypeArray arr) {
            TypeInformal element = arr.deepElement();
            if (element instanceof TypeFilled fld) {
                adapter.tconst(Type.getType(fld.type().withArguments().array(arr.depth()).descriptor()));
            }
        }
    }

    @Override
    public TypeInformal type() {
        return Types.type(Class.class);
    }

}
