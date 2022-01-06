package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
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
    public Verification<ScopeBlock> verify(Environment environment, CodeContext context) {
        return Verification.builder(this)
                .child(this.node.verify(environment, context.childScope()))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.node.apply(adapter, environment, context.childScope());
    }

}
