package honeyroasted.cello.node.ast.instruction;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.Var;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.node.ast.util.AlternativeProcessNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Optional;

public class LocalSet extends AbstractPropertyHolder implements TypedNode<LocalSet> {
    private String name;
    private TypedNode value;

    public LocalSet(String name, TypedNode value) {
        this.name = name;
        this.value = value;
    }

    private TypeInformal expected;
    private TypeInformal type;

    @Override
    public Verification<LocalSet> preprocess() {
        Verification.Builder<LocalSet> builder = Verification.builder(this);

        Verification<TypedNode> value = this.value.preprocess();
        builder.child(value);

        if (value.success() && value.value().isPresent()) {
            this.value = value.value().get();
            return builder.success(true).build();
        } else {
            return builder.noChildError().build();
        }
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
    }

    @Override
    public CodeNode<?> untyped() {
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