package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.instruction.operator.bool.BooleanOperator;
import honeyroasted.cello.verify.Verification;
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
                i.body().preprocessFully())).collect(Collectors.toList());
        return this;
    }

    @Override
    public Verification<IfBlock> verify(Environment environment, CodeContext context) {
        return Verification.builder(this)
                .children(this.ifs.stream().map(i -> {
                            CodeContext child = context.childScope();
                            return Verification.builder(i)
                                    .child(i.condition().verify(environment, child))
                                    .child(i.body().verify(environment, child))
                                    .andChildren()
                                    .build();
                        }
                ).collect(Collectors.toList()))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label end = new Label();
        for (int i = 0; i < this.ifs.size(); i++) {
            CodeContext child = context.childScope();

            If ifBlk = this.ifs.get(i);
            Label endBlk;
            if (i != this.ifs.size() - 1) {
                endBlk = new Label();
            } else {
                endBlk = end;
            }

            if (ifBlk.condition() instanceof BooleanOperator bop) {
                bop.jumpIfFalse(endBlk, adapter, environment, child);
            } else {
                ifBlk.condition().apply(adapter, environment, child);
                adapter.ifeq(endBlk);
            }

            ifBlk.body().apply(adapter, environment, child);
            adapter.goTo(end);

            if (i != this.ifs.size() - 1) {
                adapter.mark(endBlk);
            }
        }
        adapter.mark(end);
    }

    public static class If extends AbstractPropertyHolder {
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
