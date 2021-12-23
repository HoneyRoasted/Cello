package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
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
    public TypedNode preprocess() {
        this.cases.replaceAll((k, n) -> n.preprocessFully());
        return this;
    }

    @Override
    public Verification<CodeNode> verify(Environment environment, LocalScope localScope) {
        Verification.Builder<CodeNode> builder = Verification.builder();

        for (Map.Entry<Supplier<Boolean>, TypedNode> entry : this.cases.entrySet()) {
            Supplier<Boolean> test = entry.getKey();
            TypedNode node = entry.getValue();

            Verification<CodeNode> typeCheck = node.verify(environment, localScope.copy());
            builder.child(typeCheck);
            if (typeCheck.success() && test.get()) {
                node.verify(environment, localScope);
                this.chosen = node;
                return builder.orChildren().value(this).build();
            }
        }

        return builder.success(false).value(this).message("No successful type-checks passed the type cases").build();
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.chosen.apply(adapter, environment, localScope);
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
