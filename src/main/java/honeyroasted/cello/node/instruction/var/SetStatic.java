package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class SetStatic extends AbstractNode implements Node {
    private Namespace cls;
    private String name;
    @Child
    private Node value;
    private boolean dup;

    public SetStatic(Namespace cls, String name, Node value, boolean dup) {
        this.cls = cls;
        this.name = name;
        this.value = new Convert(value, (e, c) -> GetField.lookupField(this, cls, name, e, c, true).map(FieldNode::type));
        this.dup = dup;
    }

    public SetStatic(Namespace cls, String name, Node value) {
        this(cls, name, value, true);
    }

    private FieldNode target;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return GetField.lookupField(this, cls, name, environment, context, true).map(f -> {
            this.target = f;
            return this.dup ? f.type() : Types.VOID;
        });
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.value.apply(adapter, environment, context);

        if (this.dup) {
            int size = TypeUtil.size(this.value.type());
            if (size == 1) {
                adapter.dup();
            } else if (size == 2) {
                adapter.dup2();
            }
        }

        adapter.putstatic(this.cls.internalName(), this.name, this.target.type().descriptor());
    }

    @Override
    public Node toUntyped() {
        return new SetStatic(this.cls, this.name, Nodes.unwrapConvert(this.value), true);
    }
}