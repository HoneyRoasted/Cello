package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class SequenceBlock extends AbstractPropertyHolder implements CodeNode<SequenceBlock, SequenceBlock> {
    private List<CodeNode> nodes;

    public SequenceBlock(List<CodeNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public SequenceBlock preprocess() {
        this.nodes = this.nodes.stream().map(CodeNode::untyped).collect(Collectors.toList());
        return this;
    }

    @Override
    public Verification<SequenceBlock> verify(Environment environment, LocalScope localScope) {
        return Verification.builder(this)
                .children(this.nodes.stream().map(c -> (Verification<?>) c.verify(environment, localScope)).collect(Collectors.toList()))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.nodes.forEach(c -> c.apply(adapter, environment, localScope));
    }
}
