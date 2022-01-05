package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.Control;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.instruction.operator.bool.BooleanOperator;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

public class DoWhileBlock extends AbstractPropertyHolder implements CodeNode<DoWhileBlock, DoWhileBlock> {
    private String name;
    private TypedNode condition;
    private CodeNode sequence;

    public DoWhileBlock(String name, TypedNode condition, CodeNode sequence) {
        this.name = name;
        this.condition = condition;
        this.sequence = sequence;
    }

    @Override
    public DoWhileBlock preprocess() {
        if (!(this.condition instanceof BooleanOperator)) {
            this.condition = Nodes.convert(this.condition, Types.BOOLEAN).preprocessFully();
        } else {
            this.condition = this.condition.preprocessFully();
        }

        return this;
    }

    @Override
    public Verification<DoWhileBlock> verify(Environment environment, LocalScope localScope) {
        LocalScope child = localScope.child();
        child.createControl(Control.Kind.CONTINUE, this.name);
        child.createControl(Control.Kind.BREAK, this.name);

        return Verification.builder(this)
                .child(this.condition.verify(environment, child))
                .child(this.sequence.verify(environment, child))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        LocalScope child = localScope.child();
        Control cont = child.createControl(Control.Kind.CONTINUE, this.name);
        Control brk = child.createControl(Control.Kind.BREAK, this.name);

        Label start = new Label();

        adapter.mark(start);
        this.sequence.apply(adapter, environment, child);
        adapter.mark(cont.label());

        if (this.condition instanceof BooleanOperator bop) {
            bop.jumpIfTrue(start, adapter, environment, child);
        } else {
            this.condition.apply(adapter, environment, child);
            adapter.ifne(start);
        }

        if (brk.hasLabel()) {
            adapter.mark(brk.label());
        }
    }
}