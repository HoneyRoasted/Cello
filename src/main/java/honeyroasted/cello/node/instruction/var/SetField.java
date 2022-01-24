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
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class SetField extends AbstractNode implements Node {
    @Child
    private Node source;
    private String name;
    @Child
    private Node value;
    private boolean dup;

    public SetField(Node source, String name, Node value, boolean dup) {
        this.source = source;
        this.name = name;
        this.value = new Convert(value, (e, c) -> GetField.lookupField(this, source.type(), name, e, c, false).map(f -> {
            Optional<TypeClass> parent = this.source.type().supertype(f.owner().parameterizedType());
            if (parent.isPresent() && parent.get() instanceof TypeFilled fld) {
                return f.type().resolveTypeVariables(fld);
            } else {
                return f.type();
            }
        }));
        this.dup = dup;
    }

    public SetField(Node source, String name, Node value) {
        this(source, name, value, false);
    }

    private FieldNode target;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return GetField.lookupField(this, this.source.type(), this.name, environment, context, false).map(f -> {
            this.target = f;

            if (this.dup) {
                Optional<TypeClass> parent = this.source.type().supertype(f.owner().parameterizedType());
                if (parent.isPresent() && parent.get() instanceof TypeFilled fld) {
                    return f.type().resolveTypeVariables(fld);
                } else {
                    return f.type();
                }
            } else {
                return Types.VOID;
            }
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

        adapter.putfield(this.target.owner().namespace().internalName(), this.name, this.target.type().descriptor());
    }

    @Override
    public Node toUntyped() {
        return new SetField(this.source, this.name, Nodes.unwrapConvert(this.value), false).withProperties(this.properties());
    }
}
