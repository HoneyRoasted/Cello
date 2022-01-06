package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class LocalGet extends AbstractPropertyHolder implements TypedNode<LocalGet, LocalGet> {
    private String name;

    private TypeInformal expected;
    private TypeInformal type;

    public LocalGet(String name) {
        this.name = name;
    }

    @Override
    public void provideExpected(TypeInformal type) {
        this.expected = type;
    }

    @Override
    public Verification<LocalGet> verify(Environment environment, CodeContext context) {
        Optional<Var> varOpt = context.scope().fetch(this.name);
        if (varOpt.isPresent() && varOpt.get().initialized()) {
            TypeInformal type = varOpt.get().type();
            if (this.expected != null && type.isAssignableTo(expected)) {
                this.type = this.expected;
            } else {
                this.type = type;
            }

            return Verification.success(this);
        }

        return Verification.builder(this)
                .varNotFoundError(this.name)
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.load(context.scope().fetch(this.name).get().index(),
                TypeUtil.asmType(this.type));
    }

    @Override
    public TypeInformal type() {
        return this.type;
    }

}
