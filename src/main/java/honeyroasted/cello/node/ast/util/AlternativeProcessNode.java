package honeyroasted.cello.node.ast.util;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.control.ControlScope;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class AlternativeProcessNode<T extends CodeNode, K extends CodeNode> extends AbstractPropertyHolder implements CodeNode<T, AlternativeProcessNode> {
    private T value;
    private TriConsumer<InstructionAdapter, Environment, LocalScope, ControlScope> applicator;

    public AlternativeProcessNode(T value, TriConsumer<InstructionAdapter, Environment, LocalScope, ControlScope> applicator) {
        this.value = value;
        this.applicator = applicator;
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        this.applicator.accept(adapter, environment, localScope, controlScope);
    }

    @Override
    public Verification<T> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        return this.value.verify(environment, localScope, controlScope);
    }

    @Override
    public AlternativeProcessNode preprocess() {
        this.value = (T) this.value.preprocessFully();
        return this;
    }

}
