package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.environment.Control;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.verify.Verification;
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
    public Verification<Break> verify(Environment environment, LocalScope localScope) {
        if (localScope.fetchControl(Control.Kind.BREAK, this.name).isPresent()) {
            return Verification.success(this);
        }

        return Verification.builder(this)
                .controlError(this.name, "break")
                .build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        adapter.goTo(localScope.fetchControl(Control.Kind.BREAK, this.name).get().label());
    }
}
