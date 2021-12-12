package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.control.ControlScope;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.verify.Verification;
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
    public Verification<Continue> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        if (!controlScope.has(ControlScope.Kind.CONDITION, this.name)) {
            return Verification.builder(this)
                    .controlError(this.name, "continue")
                    .build();
        }
        return Verification.success(this);
    }

    @Override
    public void walkControls(ControlScope controlScope) {
        controlScope.require(ControlScope.Kind.CONDITION);
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        adapter.goTo(controlScope.fetch(this.name).get().get(ControlScope.Kind.CONDITION).label());
    }
}
