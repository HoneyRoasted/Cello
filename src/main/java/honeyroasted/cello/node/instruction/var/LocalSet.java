package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.Var;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.instruction.util.AlternativeProcessNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class LocalSet extends AbstractPropertyHolder implements TypedNode<LocalSet, LocalSet> {
    private String name;
    private TypedNode value;

    public LocalSet(String name, TypedNode value) {
        this.name = name;
        this.value = value;
    }

    private TypeInformal expected;
    private TypeInformal type;

    @Override
    public LocalSet preprocess() {
        this.value = Nodes.convert(this.value.preprocessFully(),
                (env, scope) -> scope.fetch(this.name).map(Var::type).orElse(Types.OBJECT));
        return this;
    }

    @Override
    public void provideExpected(TypeInformal type) {
        this.expected = type;
    }

    @Override
    public Verification<LocalSet> verify(Environment environment, LocalScope localScope) {
        Verification.Builder<LocalSet> builder = Verification.builder();
        builder.value(this);

        Verification<TypedNode> child = this.value.verify(environment, localScope);
        builder.child(child);

        if (child.success()) {
            Optional<Var> varOpt = localScope.fetch(this.name);
            if (varOpt.isPresent()) {
                TypeInformal type = varOpt.get().type();
                if (this.value.type().isAssignableTo(type)) {

                    if (this.expected != null && type.isAssignableTo(this.expected)) {
                        this.type = this.expected;
                    } else {
                        this.type = type;
                    }

                    varOpt.get().setInitialized(true);
                    return builder.success(true).build();
                } else {
                    return builder.success(false)
                            .typeError(this.value.type(), type)
                            .build();
                }
            }

            return builder.varNotFoundError(this.name).build();
        } else {
            return builder.andChildren().build();
        }
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        this.value.apply(adapter, environment, localScope);
        if (TypeUtil.size(this.type) == 1) {
            adapter.dup();
        } else {
            adapter.dup2();
        }
        adapter.store(localScope.fetch(this.name).get().index(),
                TypeUtil.asmType(this.type));
        localScope.fetch(this.name).get().setInitialized(true);
    }

    @Override
    public CodeNode<?, ?> untyped() {
        return new AlternativeProcessNode<>(this,
                (adapter, environment, localScope) -> {
                   this.value.apply(adapter, environment, localScope);
                   adapter.store(localScope.fetch(this.name).get().index(),
                           TypeUtil.asmType(this.type));
                });
    }

    @Override
    public TypeInformal type() {
        return this.type;
    }

}
