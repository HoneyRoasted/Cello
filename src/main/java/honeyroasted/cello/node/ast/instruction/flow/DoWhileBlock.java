package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.environment.control.Control;
import honeyroasted.cello.environment.control.SimpleControl;
import honeyroasted.cello.environment.control.ControlScope;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.ast.instruction.operator.bool.BooleanOperator;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
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
        }        this.sequence = Nodes.scope(this.sequence).preprocessFully();
        return this;
    }

    @Override
    public Verification<DoWhileBlock> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        return Verification.builder(this)
                .child(this.condition.verify(environment, localScope, controlScope.child().guaranteeAll()))
                .child(this.sequence.verify(environment, localScope, controlScope.child().guaranteeAll()))
                .andChildren()
                .build();
    }

    @Override
    public void walkControls(ControlScope controlScope) {
        this.sequence.walkControls(controlScope.child());
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        ControlScope self = controlScope.child();

        Control start = self.require(ControlScope.Kind.START);
        Control end = self.require(ControlScope.Kind.END);
        Control condition = self.get(ControlScope.Kind.CONDITION);

        adapter.mark(start.label());
        this.sequence.apply(adapter, environment, localScope, self);

        if (this.condition instanceof BooleanOperator bop) {
            bop.jumpIfTrue(start.label());
        } else {
            if (condition.hasLabel()) {
                adapter.mark(condition.label());
            }

            this.condition.apply(adapter, environment, localScope, self);
            adapter.ifne(start.label());
        }
        adapter.mark(end.label());
    }
}