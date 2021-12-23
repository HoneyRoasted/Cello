package honeyroasted.cello.node.instruction.var;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.Var;
import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.instruction.CodeNode;
import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class LocalDefine extends AbstractPropertyHolder implements CodeNode<LocalDefine, LocalDefine> {
    private TypeInformal type;
    private String name;
    private TypedNode value;

    public LocalDefine(TypeInformal type, String name, TypedNode value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public LocalDefine preprocess() {
        this.value = Nodes.convert(this.value.preprocessFully(), this.type);
        return this;
    }

    @Override
    public Verification<LocalDefine> verify(Environment environment, LocalScope localScope) {
        if (localScope.has(this.name)) {
            return Verification.builder(this)
                    .varAlreadyDefinedError(this.name)
                    .build();
        } else if (Types.VOID.equals(this.type)) {
            return Verification.builder(this)
                    .invalidTypeError(this.type)
                    .build();
        }

        if (this.value != null) {
            if (this.type != null) {
                this.value.provideExpected(this.type);
            }

            Verification<TypedNode> verification = this.value.verify(environment, localScope);
            if (verification.success()) {
                if (this.type == null) {
                    this.type = this.value.type();
                    return Verification.builder(this)
                            .success(true)
                            .child(verification)
                            .build();
                } else if (this.value.type().isAssignableTo(this.type)) {
                    return Verification.builder(this)
                            .success(true)
                            .child(verification)
                            .build();
                } else {
                    return Verification.builder(this)
                            .typeError(this.value.type(), this.type)
                            .child(verification)
                            .build();
                }
            } else {
                return Verification.builder(this)
                        .child(verification)
                        .noChildError()
                        .build();
            }
        } else if (this.type == null || this.type.equals(Types.VOID)) {
               return Verification.builder(this)
                       .invalidTypeError(this.type)
                       .build();
        } else {
            return Verification.success(this);
        }
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        Var var = localScope.define(this.name, this.type).get();
        if (this.value != null) {
            localScope.fetch(this.name).get().setInitialized(true);
            this.value.apply(adapter, environment, localScope);
            adapter.store(var.index(), TypeUtil.asmType(var.type()));
        }
    }

}
