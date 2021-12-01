package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.Scope;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TypeCaseNode extends AbstractPropertyHolder implements TypedNode {
    private Map<Supplier<Boolean>, TypedNode> cases;

    public TypeCaseNode(Map<Supplier<Boolean>, TypedNode> cases) {
        this.cases = cases;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void provideExpected(TypeInformal type) {
        this.cases.values().forEach(t -> t.provideExpected(type));
    }

    private TypedNode chosen;

    @Override
    public Verification<CodeNode> typeCheck(Environment environment, Scope scope) {
        Verification.Builder<CodeNode> builder = Verification.builder();

        for (Map.Entry<Supplier<Boolean>, TypedNode> entry : this.cases.entrySet()) {
            Supplier<Boolean> test = entry.getKey();
            TypedNode node = entry.getValue();

            Verification<CodeNode> typeCheck = node.typeCheck(environment, scope.copy());
            builder.child(typeCheck);
            if (typeCheck.success() && test.get()) {
                node.typeCheck(environment, scope);
                this.chosen = node;
                return builder.orChildren().value(this).build();
            }
        }

        return builder.success(false).value(this).message("No successful type-checks passed the type cases").build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, Scope scope) {
        this.chosen.apply(adapter, environment, scope);
    }

    @Override
    public TypeInformal type() {
        return this.chosen.type();
    }

    public static class Builder {
        private Map<Supplier<Boolean>, TypedNode> cases = new LinkedHashMap<>();

        public Builder typeCase(Supplier<Boolean> test, TypedNode node) {
            this.cases.put(test, node);
            return this;
        }

        public TypeCaseNode build() {
            return new TypeCaseNode(new LinkedHashMap<>(this.cases));
        }

    }

}
