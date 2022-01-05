package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.javatype.Types;
import honeyroasted.javatype.method.TypeMethodParameterized;
import honeyroasted.javatype.parameterized.TypeVar;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

public class MethodSignatureVisitor extends CelloSignatureVisitor<TypeMethodParameterized> {
    private TypeVarScope scope;

    private TypeMethodParameterized.Builder type = Types.method();

    private TypeVar previous;
    private TypeVar.Builder previousBuilder;

    public MethodSignatureVisitor(Consumer<TypeMethodParameterized> end, TypeVarScope scope) {
        super(end);
        this.scope = scope;
    }

    public MethodSignatureVisitor(TypeVarScope scope) {
        this.scope = scope;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        if (this.previous != null) {
            this.previousBuilder.build(this.previous);
        }

        this.previous = this.scope.define(name);
        this.previousBuilder = Types.var();
        this.type.addTypeParameter(this.previous);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.previousBuilder.addBound(f), this.scope));
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.previousBuilder.addBound(f), this.scope));
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.type.addParameter(f), this.scope));
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.type.returnType(f), this.scope));
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.type.addException(f), this.scope));
    }

    @Override
    protected void finish() {
        if (this.previous != null) {
            this.previousBuilder.build(this.previous);
        }
        setValue(this.type.build());
    }

}
