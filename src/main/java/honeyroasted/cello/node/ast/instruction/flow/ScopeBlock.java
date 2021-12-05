package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.environment.control.ControlScope;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.verify.Verification;
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
    public Verification<ScopeBlock> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        return Verification.builder(this)
                .child(this.node.verify(environment, localScope.child(), controlScope))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        this.node.apply(adapter, environment, localScope.child(), controlScope);
    }

}
