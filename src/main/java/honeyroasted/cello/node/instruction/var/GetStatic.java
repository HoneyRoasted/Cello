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

    public static Verification<FieldNode> lookupStaticField(Namespace cls, String name, Environment environment, CodeContext context) {
        VerificationBuilder<FieldNode> builder = Verification.builder();

        Verification<ClassNode> lookup = environment.lookup(cls);
        builder.child(lookup);

        if (lookup.success() && lookup.value().isPresent()) {
            ClassNode node = lookup.value().get();
            List<FieldNode> fields = node.lookupFields(f -> f.name().equals(name));

            if (fields.isEmpty()) {
                return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' not found", cls.name(), name).build();
            }

            fields = fields.stream().filter(f -> f.modifiers().has(Modifier.STATIC)).toList();

            if (fields.isEmpty()) {
                return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' is not static", cls.name(), name).build();
            }

            fields = fields.stream().filter(f -> context.owner().owner().accessTo(f.owner()).canAccess(f.modifiers().access())).toList();

            if (fields.isEmpty()) {
                return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' is not accessible from class '%s'", cls.name(), name,
                        context.owner().owner().parameterizedType().namespace().name()).build();
            }

            if (fields.size() > 1 && !fields.get(0).owner().equals(node)) {
                return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "'%s#%s' is ambiguous, possible fields in %s",
                        cls.name(), name, fields.stream().map(f -> f.owner().parameterizedType().namespace().name()).toList()).build();
            }

            builder.value(fields.get(0));
        }

        return builder.andChildren().build();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return lookupStaticField(this.cls, this.name, environment, context).map(f -> {
            this.target = f;
            return f.type();
        });
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        adapter.getstatic(this.cls.internalName(), this.name, this.target.type().descriptor());
    }

}
