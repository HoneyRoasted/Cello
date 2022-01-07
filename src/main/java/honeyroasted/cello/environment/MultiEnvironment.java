package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiEnvironment extends AbstractCachingEnvironment {
    private List<Environment> environments = new ArrayList<>();

    public MultiEnvironment add(Environment... environments) {
        Collections.addAll(this.environments, environments);
        return this;
    }

    @Override
    protected Verification<ClassNode> performLookup(Namespace namespace) {
        VerificationBuilder<ClassNode> builder = Verification.builder();
        builder.source(this);

        for (Environment environment : this.environments) {
            Verification<ClassNode> lookup = environment.lookup(namespace);
            builder.child(lookup);
            if (lookup.success() && lookup.value().isPresent()) {
                return builder.value(lookup.value().get()).build();
            }
        }

        return builder.error(Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not resolve class '%s'", namespace.name()).build();
    }

}
