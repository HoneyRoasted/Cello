package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.NoOp;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class Return extends AbstractNode implements Node {
    @Child
    private Node value;

    public Return(Node value) {
        value = value == null ? new NoOp() : value;
        this.value = Nodes.convert(value, (e, c) -> c.owner().returnType());
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return Verification.success(this, Types.VOID);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        if (context.withinTryFinally()) {
            Var delayed = context.common().delayedReturn().orElse(context.scope().define("#return", context.owner().erased().returnType()));
            context.common().delayedReturn(delayed);

            delayed.setInitialized(true);
            adapter.store(delayed.index(), TypeUtil.asmType(delayed.type()));

            Optional<Control> control = context.scope().fetchControl(Control.Kind.FINALLY);
            if (control.isPresent()) {
                adapter.goTo(control.get().label());
            }
        } else {
            if (this.value.type().equals(Types.VOID)) {
                adapter.visitInsn(Opcodes.RETURN);
            } else {
                this.value.apply(adapter, environment, context);
                adapter.areturn(TypeUtil.asmType(this.value.type()));
            }
        }
    }

    @Override
    public boolean terminal() {
        return true;
    }
}
