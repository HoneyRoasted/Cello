package honeyroasted.cello.node.instruction.invoke;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;

public class InvokeSpecial extends AbstractNode implements Node {
    @Child
    private Node source;
    private Namespace sourceType;
    private String name;
    @Child
    private List<Node> parameters;

    public InvokeSpecial(Node source, Namespace sourceType, String name, List<Node> parameters) {
        this.source = source;
        this.sourceType = sourceType;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return null;
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {

    }

}
