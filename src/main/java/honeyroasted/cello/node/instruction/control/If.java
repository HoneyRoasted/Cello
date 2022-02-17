package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
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
    @Child(scope = Child.SUB_SCOPE)
    private Node elseBlock;

    public If(List<IfBlock> blocks, Node elseBlock) {
        this.blocks = blocks;
        this.elseBlock = elseBlock != null ? elseBlock.toUntyped() : elseBlock;
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
        Label elseBlock = new Label();

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
                block.condition().jumpIfFalse(this.elseBlock == null ? end : elseBlock, adapter, environment, child);
            }
        }

        if (this.elseBlock != null) {
            adapter.mark(elseBlock);
            this.elseBlock.apply(adapter, environment, context);
        }

        adapter.mark(end);
    }

    @Override
    public boolean terminal() {
        return this.elseBlock != null && this.elseBlock.terminal() && this.blocks.stream().allMatch(i -> i.body().terminal());
    }

    public static class IfBlock {
        private Convert condition;
        private Node body;

        public IfBlock(Node condition, Node body) {
            this.condition = Nodes.convert(condition, Types.BOOLEAN);
            this.body = body.toUntyped();
        }

        public Convert condition() {
            return condition;
        }

        public Node body() {
            return body;
        }
    }

}
