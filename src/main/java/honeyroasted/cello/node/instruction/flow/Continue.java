package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class Continue extends AbstractPropertyHolder implements CodeNode<Continue, Continue> {
    private String name;

    public Continue(String name) {
        this.name = name;
    }

    public Continue() {
        this(null);
    }

    @Override
    public Verification<Continue> verify(Environment environment, CodeContext context) {
        if (context.scope().fetchControl(Control.Kind.CONTINUE, this.name).isPresent()) {
            return Verification.success(this);
        }

        return Verification.builder(this)
                .controlError(this.name, "break")
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.goTo(context.scope().fetchControl(Control.Kind.CONTINUE, this.name).get().label());
    }
}
