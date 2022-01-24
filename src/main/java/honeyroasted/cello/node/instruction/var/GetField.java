package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GetField extends AbstractNode implements Node {
    @Child
    private Node source;
    private String name;

    public GetField(Node source, String name) {
        this.source = source;
        this.name = name;
    }

    private FieldNode target;

    public static Verification<FieldNode> lookupInstanceField(Node owner, Node source, String name, Environment environment, CodeContext context) {
        VerificationBuilder<FieldNode> builder = Verification.builder();
        builder.source(owner);

        Set<TypeClass> types = TypeUtil.flatten(source.type());
        List<List<FieldNode>> fieldCandidates = new ArrayList<>();

        for (TypeClass type : types) {
            Verification<ClassNode> lookup = environment.lookup(type);
            builder.child(lookup);

            if (lookup.success() && lookup.value().isPresent()) {
                fieldCandidates.add(lookup.value().get().lookupFields(f -> f.name().equals(name)));
            }
        }

        int max = fieldCandidates.stream().mapToInt(List::size).max().orElse(0);

        List<FieldNode> fields = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            for (List<FieldNode> nodes : fieldCandidates) {
                if (i < nodes.size() && !fields.contains(nodes.get(i))) {
                    fields.add(nodes.get(i));
                }
            }
        }

        if (fields.isEmpty()) {
            return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' not found", types.stream().map(Type::externalName).toList(), name).build();
        }

        fields = fields.stream().filter(f -> !f.modifiers().has(Modifier.STATIC)).toList();

        if (fields.isEmpty()) {
            return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' is static", types.stream().map(Type::externalName).toList(), name).build();
        }

        fields = fields.stream().filter(f -> context.owner().owner().accessTo(f.owner()).canAccess(f.modifiers().access())).toList();

        if (fields.isEmpty()) {
            return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "Field '%s#%s' is not accessible from class '%s'",
                    types.stream().map(Type::externalName).toList(), name,
                    context.owner().owner().externalName()).build();
        }

        if (fields.size() > 1) {
            return builder.error(Verify.Code.FIELD_NOT_FOUND_ERROR, "'%s' is ambiguous, possible fields in %s",
                    name, fields.stream().map(f -> f.owner().externalName()).toList()).build();
        }

        return builder.value(fields.get(0)).andChildren().build();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        return lookupInstanceField(this, this.source, this.name, environment, context).map(f -> {
            this.target = f;

            Optional<TypeClass> parent = this.source.type().supertype(f.owner().parameterizedType());
            if (parent.isPresent() && parent.get() instanceof TypeFilled fld) {
                return f.type().resolveTypeVariables(fld);
            } else {
                return f.type();
            }
        });
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.source.apply(adapter, environment, context);
        adapter.getfield(this.target.owner().namespace().internalName(), this.name,this.target.type().descriptor());
    }
}
