package honeyroasted.cello.node.instruction.value;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.commons.InstructionAdapter;

public class LoadThis extends AbstractPropertyHolder implements TypedNode<LoadThis, LoadThis> {
    private TypeInformal type;

    @Override
    public Verification<LoadThis> verify(Environment environment, CodeContext context) {
        if (context.owner().modifiers().has(Modifier.STATIC)) {
            return Verification.builder(this)
                    .thisNotAvailable()
                    .build();
        } else {
            TypeParameterized clsType = context.owner().owner().type();

            TypeFilled.Builder builder = Types.filled().type(clsType);
            clsType.typeParameters().forEach(t -> builder.addGeneric(Types.ref(t)));

            this.type = builder.build();
        }

        return Verification.success(this);
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.load(0, TypeUtil.asmType(this.type));
    }

    @Override
    public TypeInformal type() {
        return this.type;
    }

}
