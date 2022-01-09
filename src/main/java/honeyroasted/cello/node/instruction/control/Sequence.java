package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class Sequence extends AbstractNode implements Node {
    @Child
    private List<Node> nodes;

    public Sequence(List<Node> nodes) {
        this.nodes = nodes.stream().map(Node::toUntyped).collect(Collectors.toList());
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return Verification.success(this, Types.VOID);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.nodes.forEach(n -> n.apply(adapter, environment, context));
    }
}
