package honeyroasted.cello.node.instruction.operator;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.BooleanValue;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.informal.TypeVarRef;
import honeyroasted.javatype.parameterized.TypeVar;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;

public class Ternary extends AbstractNode implements Node {
    @Child
    private Node condition;
    @Child(order = Child.BOTH)
    private Node left;
    @Child(order = Child.BOTH)
    private Node right;

    public Ternary(Node condition, Node left, Node right) {
        this.condition = Nodes.convert(condition, Types.BOOLEAN);
        this.left = left;
        this.right = right;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        TypeVarRef var = TypeVar.builder()
                .bounds(new ArrayList<>(Types.commonParents(this.left.type(), this.right.type())))
                .name("_")
                .build().ref();

        this.left = Nodes.convert(this.left, var);
        this.right = Nodes.convert(this.right, var);

        return Verification.success(this, var);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label right = new Label();
        Label end = new Label();

        if (this.condition instanceof BooleanValue bop) {
            bop.jumpIfFalse(right, adapter, environment, context);
        } else {
            this.condition.apply(adapter, environment, context);
            adapter.ifeq(right);
        }

        this.left.apply(adapter, environment, context);
        adapter.goTo(end);
        adapter.mark(right);
        this.right.apply(adapter, environment, context);
        adapter.mark(end);
    }
}
