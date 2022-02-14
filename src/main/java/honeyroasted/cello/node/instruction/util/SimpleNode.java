package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public class SimpleNode extends AbstractNode implements Node {
    private BiFunction<Environment, CodeContext, Verification<TypeInformal>> verify;
    private TriConsumer<InstructionAdapter, Environment, CodeContext> apply;

    public SimpleNode(BiFunction<Environment, CodeContext, Verification<TypeInformal>> verify, TriConsumer<InstructionAdapter, Environment, CodeContext> apply) {
        this.verify = verify;
        this.apply = apply;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return this.verify.apply(environment, context);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.apply.accept(adapter, environment, context);
    }

}
