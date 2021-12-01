package honeyroasted.cello.node.ast.util;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class AlternativeProcessNode<T extends CodeNode> extends AbstractPropertyHolder implements CodeNode<T> {
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
        Verification<T> verification = this.value.verify(environment, localScope);
        if (verification.success() && verification.value().isPresent()) {
            this.value = verification.value().get();
        }
        return verification;
    }

    @Override
    public Verification<T> preprocess() {
        Verification<T> verification = this.value.preprocess();
        if (verification.success() && verification.value().isPresent()) {
            this.value = verification.value().get();
        }
        return verification;
    }

}