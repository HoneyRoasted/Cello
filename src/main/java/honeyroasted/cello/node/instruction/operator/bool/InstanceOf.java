package honeyroasted.cello.node.instruction.operator.bool;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class InstanceOf extends AbstractNode implements Node {
    @Child
    private Node node;
    private Namespace clsType;

    public InstanceOf(Node node, Namespace clsType) {
        this.node = node;
        this.clsType = clsType;
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        Verification<ClassNode> lookup = environment.lookup(clsType);

        if (lookup.success()) {
            if (this.node.type().isPrimitive()) {
                return Verification.<TypeInformal>builder()
                        .child(lookup)
                        .error(Verify.Code.INVALID_OPERATOR, "Cannot apply operator %s to type %s", "instanceof", this.node.type())
                        .build();
            } else {
                return Verification.<TypeInformal>builder(Types.BOOLEAN)
                        .source(this)
                        .child(lookup)
                        .build();
            }
        } else {
            return Verification.<TypeInformal>builder()
                    .child(lookup)
                    .andChildren()
                    .build();
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.instanceOf(TypeUtil.asmType(this.clsType));
    }

}
