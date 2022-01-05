package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class ScopeBlock extends AbstractPropertyHolder implements CodeNode<ScopeBlock, ScopeBlock> {
    private CodeNode node;

    public ScopeBlock(CodeNode node) {
        this.node = node;
    }

    @Override
    public ScopeBlock preprocess() {
        this.node = this.node.untyped().preprocessFully();
        return this;
    }

    @Override
    public Verification<ScopeBlock> verify(Environment environment, LocalScope localScope) {
        return Verification.builder(this)
                .child(this.node.verify(environment, localScope.child()))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.node.apply(adapter, environment, localScope.child());
    }

}
