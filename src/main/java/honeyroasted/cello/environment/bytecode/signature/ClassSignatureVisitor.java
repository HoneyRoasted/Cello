package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.parameterized.TypeParameterized;
import honeyroasted.javatype.parameterized.TypeVar;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

public class ClassSignatureVisitor extends CelloSignatureVisitor<TypeParameterized> {
    private Namespace namespace;
    private TypeVarScope scope;

    private TypeParameterized.Builder type = Types.parameterized();

    private TypeVar previous;
    private TypeVar.Builder previousBuilder;

    public ClassSignatureVisitor(Consumer<TypeParameterized> end, Namespace namespace, TypeVarScope scope) {
        super(end);
        this.namespace = namespace;
        this.scope = scope;
    }

    public ClassSignatureVisitor(Namespace namespace, TypeVarScope scope) {
        this.namespace = namespace;
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
    public SignatureVisitor visitSuperclass() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.type.superclass((TypeFilled) f), this.scope));
    }

    @Override
    public SignatureVisitor visitInterface() {
        return this.logAndReturn(new TypeSignatureVisitor(f -> this.type.addInterface((TypeFilled) f), this.scope));
    }

    @Override
    protected void finish() {
        if (this.previous != null) {
            this.previousBuilder.build(this.previous);
        }
        this.type.namespace(this.namespace);
        setValue(this.type.build());
    }
}
