package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class AlternativeProcessNode<T extends CodeNode, K extends CodeNode> extends AbstractPropertyHolder implements CodeNode<T, AlternativeProcessNode> {
    private T value;
    private TriConsumer<InstructionAdapter, Environment, CodeContext> applicator;

    public AlternativeProcessNode(T value, TriConsumer<InstructionAdapter, Environment, CodeContext> applicator) {
        this.value = value;
        this.applicator = applicator;
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.applicator.accept(adapter, environment, context);
    }

    @Override
    public Verification<T> verify(Environment environment, CodeContext context) {
        return this.value.verify(environment, context);
    }

    @Override
    public AlternativeProcessNode preprocess() {
        this.value = (T) this.value.preprocessFully();
        return this;
    }

}
