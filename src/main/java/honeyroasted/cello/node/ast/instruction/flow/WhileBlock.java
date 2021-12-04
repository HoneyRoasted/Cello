package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class WhileBlock extends AbstractPropertyHolder implements CodeNode<WhileBlock, WhileBlock> {
    private TypedNode condition;
    private CodeNode sequence;

    public WhileBlock(TypedNode condition, CodeNode sequence) {
        this.condition = condition;
        this.sequence = sequence;
    }

    @Override
    public WhileBlock preprocess() {
        this.condition = Nodes.convert(this.condition, Types.BOOLEAN).preprocessFully();
        this.sequence = Nodes.scope(this.sequence).preprocessFully();
        return this;
    }

    @Override
    public Verification<WhileBlock> verify(Environment environment, LocalScope localScope) {
        return Verification.builder(this)
                .child(this.condition.verify(environment, localScope))
                .child(this.sequence.verify(environment, localScope))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        Label start = new Label();
        Label end = new Label();

        adapter.mark(start);
        this.condition.apply(adapter, environment, localScope);
        adapter.ifeq(end);

        this.sequence.apply(adapter, environment, localScope);
        adapter.goTo(start);
        adapter.mark(end);
    }
}
