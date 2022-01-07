package honeyroasted.cello.environment;

import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
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
        Verification.Builder<ClassNode> builder = Verification.builder();

        for (Environment environment : this.environments) {
            Verification<ClassNode> lookup = environment.lookup(namespace);
            builder.child(lookup);
            if (lookup.isPresent()) {
                return builder.value(lookup.value()).build();
            }
        }

        return builder.typeNotFoundError(namespace).build();
    }

}
