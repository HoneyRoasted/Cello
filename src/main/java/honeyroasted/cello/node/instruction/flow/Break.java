package honeyroasted.cello.node.instruction.flow;

import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import org.objectweb.asm.commons.InstructionAdapter;

public class Break extends AbstractPropertyHolder implements CodeNode<Break, Break> {
    private String name;

    public Break(String name) {
        this.name = name;
    }

    public Break() {
        this(null);
    }

    @Override
    public Verification<Break> verify(Environment environment, CodeContext context) {
        if (context.scope().fetchControl(Control.Kind.BREAK, this.name).isPresent()) {
            return Verification.success(this);
        }

        return Verification.builder(this)
                .controlError(this.name, "break")
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.goTo(context.scope().fetchControl(Control.Kind.BREAK, this.name).get().label());
    }
}
