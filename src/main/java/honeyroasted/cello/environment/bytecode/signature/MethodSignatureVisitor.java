package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.verify.Verification;
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
    private Environment environment;

    public MethodSignatureVisitor(Consumer<Verification<TypeMethodParameterized>> end, TypeVarScope scope, Environment environment) {
        super(end);
        this.scope = scope;
        this.environment = environment;
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
        this.previousBuilder = Types.var().name(name);
        this.type.addTypeParameter(this.previous);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.previousBuilder.addBound(v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.previousBuilder.addBound(v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.type.addParameter(v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.type.returnType(v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.type.addException(v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    protected void finish() {
        if (this.previous != null) {
            this.previousBuilder.build(this.previous);
        }
        setValue(this.type.build());
        this.builder().andChildren();
    }

}
