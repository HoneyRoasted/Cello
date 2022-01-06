package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.instruction.operator.bool.BooleanOperator;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import org.objectweb.asm.commons.InstructionAdapter;

public class WhileBlock extends AbstractPropertyHolder implements CodeNode<WhileBlock, WhileBlock> {
    private String name;
    private TypedNode condition;
    private CodeNode sequence;

    public WhileBlock(String name, TypedNode condition, CodeNode sequence) {
        this.condition = condition;
        this.sequence = sequence;
        this.name = name;
    }

    @Override
    public WhileBlock preprocess() {
        if (!(this.condition instanceof BooleanOperator)) {
            this.condition = Nodes.convert(this.condition, Types.BOOLEAN).preprocessFully();
        } else {
            this.condition = this.condition.preprocessFully();
        }
        return this;
    }

    @Override
    public Verification<WhileBlock> verify(Environment environment, CodeContext context) {
        CodeContext child = context.childScope();
        child.scope().createControl(Control.Kind.CONTINUE, this.name);
        child.scope().createControl(Control.Kind.BREAK, this.name);

        return Verification.builder(this)
                .child(this.condition.verify(environment, child))
                .child(this.sequence.verify(environment, child))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        CodeContext child = context.childScope();
        Control cont = child.scope().createControl(Control.Kind.CONTINUE, this.name);
        Control brk = child.scope().createControl(Control.Kind.BREAK, this.name);

        adapter.mark(cont.label());
        if (this.condition instanceof BooleanOperator bop) {
            bop.jumpIfFalse(brk.label(), adapter, environment, child);
        } else {
            this.condition.apply(adapter, environment, child);
            adapter.ifeq(brk.label());
        }

        this.sequence.apply(adapter, environment, child);
        adapter.mark(brk.label());
    }
}
