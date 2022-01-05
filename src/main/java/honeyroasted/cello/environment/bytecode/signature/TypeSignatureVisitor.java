package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.cello.environment.Environment;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.informal.TypeVarRef;
import honeyroasted.javatype.informal.TypeWild;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

public class TypeSignatureVisitor extends CelloSignatureVisitor<TypeInformal> {
    private TypeVarScope scope;

    private TypeFilled.Builder filled = null;
    private TypeWild.Builder wild = null;

    private TypeInformal type = null;

    public TypeSignatureVisitor(Consumer<TypeInformal> end, TypeVarScope scope) {
        super(end);
        this.scope = scope;
    }

    public TypeSignatureVisitor(TypeVarScope scope) {
        this.scope = scope;
    }

    @Override
    public void visitBaseType(char descriptor) {
        this.type = Types.primitives().stream().filter(t -> t.descriptor().equals(String.valueOf(descriptor))).findFirst().get();
        this.setValue(this.type);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.type = Types.ref(this.scope.fetchOrPut(name));
        this.setValue(this.type);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return logAndReturn(new TypeSignatureVisitor(f -> this.type = f.array(1), this.scope));
    }

    @Override
    public void visitClassType(String name) {
        this.filled = Types.filled()
                .type(Types.parameterized().name(name).superclass(Types.OBJECT).build());
    }

    @Override
    public void visitTypeArgument() {
        if (this.filled != null) {
            this.filled.addGeneric(Types.wild().build());
        }
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        if (this.filled != null) {
            if (wildcard != '=') {
                return logAndReturn(new TypeSignatureVisitor(f -> {
                    TypeWild wild;
                    if (wildcard == '-') {
                        wild = Types.wild().lower(f).build();
                    } else {
                        wild = Types.wild().upper(f).build();
                    }
                    this.filled.addGeneric(wild);
                }, this.scope));
            } else {
                return logAndReturn(new TypeSignatureVisitor(f -> this.filled.addGeneric(f), this.scope));
            }
        }
        return super.visitTypeArgument(wildcard);
    }

    @Override
    public void finish() {
        if (this.value() == null) {
            if (this.filled != null) {
                this.setValue(this.filled.build());
            } else if (this.wild != null) {
                this.setValue(this.wild.build());
            }
        }
    }
}
