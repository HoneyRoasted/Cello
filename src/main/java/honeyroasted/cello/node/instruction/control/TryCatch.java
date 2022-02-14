package honeyroasted.cello.node.instruction.control;

import com.sun.jdi.InvalidStackFrameException;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.Set;

public class TryCatch extends AbstractNode implements Node {
    private Node body;
    private List<CatchBlock> catchBlocks;
    private Node finalBlock;

    public TryCatch(Node body, List<CatchBlock> catchBlocks, Node finalBlock) {
        this.body = body.toUntyped();
        this.catchBlocks = catchBlocks;
        this.finalBlock = finalBlock.toUntyped();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return null;
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {

    }

    public static class CatchBlock {
        private Set<TypeParameterized> type;
        private String varName;
        private Node body;

        public CatchBlock(Set<TypeParameterized> type, String varName, Node body) {
            this.type = type;
            this.varName = varName;
            this.body = body.toUntyped();
        }

        public Set<TypeParameterized> type() {
            return type;
        }

        public String varName() {
            return varName;
        }

        public Node body() {
            return body;
        }
    }

}
