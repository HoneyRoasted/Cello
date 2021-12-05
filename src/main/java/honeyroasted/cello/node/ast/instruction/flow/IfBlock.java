package honeyroasted.cello.node.ast.instruction.flow;

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
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class IfBlock extends AbstractPropertyHolder implements CodeNode<IfBlock, IfBlock> {
    private List<If> ifs;

    public IfBlock(List<If> ifs) {
        this.ifs = ifs;
    }

    @Override
    public IfBlock preprocess() {
        this.ifs = this.ifs.stream().map(i -> new If(i.condition() instanceof BooleanOperator ?
                i.condition().preprocessFully() : Nodes.convert(i.condition(), Types.BOOLEAN).preprocessFully(),
                Nodes.scope(i.body()).preprocessFully())).collect(Collectors.toList());
        return this;
    }

    @Override
    public Verification<IfBlock> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        return Verification.builder(this)
                .children(this.ifs.stream().map(i ->
                        Verification.builder(i)
                                .child(i.condition().verify(environment, localScope, controlScope))
                                .child(i.body().verify(environment, localScope, controlScope))
                                .andChildren()
                                .build()
                ).collect(Collectors.toList()))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        Label end = new Label();
        for (int i = 0; i < this.ifs.size(); i++) {
            If ifBlk = this.ifs.get(i);
            Label endBlk;
            if (i != this.ifs.size() - 1) {
                endBlk = new Label();
            } else {
                endBlk = end;
            }

            if (ifBlk.condition() instanceof BooleanOperator bop) {
                bop.jumpIfFalse(endBlk);
            } else {
                ifBlk.condition().apply(adapter, environment, localScope, controlScope);
                adapter.ifeq(endBlk);
            }

            ifBlk.body().apply(adapter, environment, localScope, controlScope);
            adapter.goTo(end);

            if (i != this.ifs.size() - 1) {
                adapter.mark(endBlk);
            }
        }
        adapter.mark(end);
    }

    public static class If {
        private TypedNode condition;
        private CodeNode body;

        public If(TypedNode condition, CodeNode body) {
            this.condition = condition;
            this.body = body;
        }

        public TypedNode condition() {
            return this.condition;
        }

        public CodeNode body() {
            return this.body;
        }
    }

}
