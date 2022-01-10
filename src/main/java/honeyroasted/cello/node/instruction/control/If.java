package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;

public class If extends AbstractNode implements Node {
    private List<IfBlock> blocks;

    public If(List<IfBlock> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        VerificationBuilder<TypeInformal> builder = Verification.builder();
        builder.source(this).value(Types.VOID);

        this.blocks.forEach(b -> {
            CodeContext child = context.childScope();
            builder.children(b.condition().verify(environment, child),
                    b.body().verify(environment, child));
        });

        return builder.andChildren().build();
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label end = new Label();

        List<IfBlock> ifBlocks = this.blocks;
        for (int i = 0; i < ifBlocks.size(); i++) {
            IfBlock block = ifBlocks.get(i);
            CodeContext child = context.childScope();

            if (i < ifBlocks.size() - 1) {
                Label blockEnd = new Label();
                block.condition().jumpIfFalse(blockEnd, adapter, environment, child);
                adapter.goTo(end);
                adapter.mark(blockEnd);
            } else {
                block.condition().jumpIfFalse(end, adapter, environment, child);
            }
        }

        adapter.mark(end);
    }

    public static class IfBlock {
        private Convert condition;
        private Node body;

        public IfBlock(Node condition, Node body) {
            this.condition = Nodes.convert(condition, Types.BOOLEAN);
            this.body = body;
        }

        public Convert condition() {
            return condition;
        }

        public Node body() {
            return body;
        }
    }

}
