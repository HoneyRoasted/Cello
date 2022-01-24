package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.modifier.Access;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.modifier.Modifiers;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class GetStatic extends AbstractNode implements Node {
    private Namespace cls;
    private String name;

    public GetStatic(Namespace cls, String name) {
        this.cls = cls;
        this.name = name;
    }

    private FieldNode target;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return GetField.lookupField(this, this.cls, this.name, environment, context, true).map(f -> {
            this.target = f;
            return f.type();
        });
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.getstatic(this.cls.internalName(), this.name, this.target.type().descriptor());
    }

}
