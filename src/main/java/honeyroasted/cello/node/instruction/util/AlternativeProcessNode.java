package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class AlternativeProcessNode<T extends CodeNode, K extends CodeNode> extends AbstractPropertyHolder implements CodeNode<T, AlternativeProcessNode> {
    private T value;
    private TriConsumer<InstructionAdapter, Environment, LocalScope> applicator;

    public AlternativeProcessNode(T value, TriConsumer<InstructionAdapter, Environment, LocalScope> applicator) {
        this.value = value;
        this.applicator = applicator;
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.applicator.accept(adapter, environment, localScope);
    }

    @Override
    public Verification<T> verify(Environment environment, LocalScope localScope) {
        return this.value.verify(environment, localScope);
    }

    @Override
    public AlternativeProcessNode preprocess() {
        this.value = (T) this.value.preprocessFully();
        return this;
    }

}
