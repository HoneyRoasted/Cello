package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Control;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class DoWhile extends AbstractNode implements Node {
    private String name;
    private Convert condition;
    private Node body;

    public DoWhile(String name, Node condition, Node body) {
        this.name = name;
        this.condition = Nodes.convert(condition, Types.BOOLEAN);
        this.body = body.toUntyped();
    }

    public DoWhile(Node condition, Node body) {
        this(null, condition, body);
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        CodeContext child = context.childScope();

        child.scope().createControl(Control.Kind.BREAK, this.name);
        child.scope().createControl(Control.Kind.CONTINUE, this.name);

        Verification<TypeInformal> a = this.condition.verify(environment, child);
        Verification<TypeInformal> b = this.body.verify(environment, child);

        return Verification.<TypeInformal>builder(Types.VOID)
                .children(a, b)
                .andChildren().build();
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        CodeContext child = context.childScope();

        child.scope().createControl(Control.Kind.BREAK, this.name, child.scope().end());
        child.scope().createControl(Control.Kind.CONTINUE, this.name, child.scope().start());

        adapter.mark(child.scope().start());
        this.body.apply(adapter, environment, child);
        this.condition.jumpIfTrue(child.scope().start(), adapter, environment, child);
        adapter.mark(child.scope().end());
    }
}
