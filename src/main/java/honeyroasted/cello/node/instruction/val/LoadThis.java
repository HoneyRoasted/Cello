package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.stream.Collectors;

public class LoadThis extends AbstractNode implements Node {

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        if (context.owner().modifiers().has(Modifier.STATIC)) {
            return Verification.error(this, Verify.Code.THIS_NOT_AVAILABLE_ERROR, "'this' not available in static context");
        } else {
            ClassNode owner = context.owner().owner();
            return Verification.success(this, owner.type(owner.parameterizedType().typeParameters().stream().map(Types::ref).collect(Collectors.toList())));
        }
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.load(0, TypeUtil.asmType(this.type()));
    }

}
