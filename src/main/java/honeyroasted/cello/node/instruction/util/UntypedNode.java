package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class UntypedNode extends AbstractPropertyHolder implements CodeNode<UntypedNode, UntypedNode> {
    private CodeNode node;

    public UntypedNode(CodeNode node) {
        this.node = node;
    }

    @Override
    public UntypedNode preprocess() {
        this.node = this.node.preprocessFully();
        return this;
    }

    @Override
    public Verification<UntypedNode> verify(Environment environment, CodeContext context) {
        return Verification.builder(this)
                .child(this.node.verify(environment, context))
                .andChildren()
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.node.apply(adapter, environment, context);
        if (this.node instanceof TypedNode) {
            TypeInformal type = ((TypedNode<?, ?>) this.node).type();
            int size = TypeUtil.size(type);

            if (size % 2 == 1) {
                adapter.pop();
            }

            for (int i = 0; i < size / 2; i++) {
                adapter.pop2();
            }
        }
    }

    @Override
    public CodeNode<?, ?> untyped() {
        return this;
    }
}
