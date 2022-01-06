package honeyroasted.cello.environment.bytecode.visitor.signature;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeVarScope;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.parameterized.TypeParameterized;
import honeyroasted.javatype.parameterized.TypeVar;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.List;
import java.util.function.Consumer;

public class ClassSignatureVisitor extends CelloSignatureVisitor<TypeParameterized> {
    private Namespace namespace;
    private TypeVarScope scope;

    private TypeParameterized superclass;
    private List<TypeParameterized> interfaces;

    private TypeParameterized.Builder type = Types.parameterized();

    private TypeVar previous;
    private TypeVar.Builder previousBuilder;

    private Environment environment;

    public ClassSignatureVisitor(Consumer<Verification<TypeParameterized>> end, Namespace namespace, TypeVarScope scope, TypeParameterized superclass, List<TypeParameterized> interfaces, Environment environment) {
        super(end);
        this.namespace = namespace;
        this.scope = scope;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.environment = environment;
    }

    public ClassSignatureVisitor(Namespace namespace, TypeVarScope scope, TypeParameterized superclass, List<TypeParameterized> interfaces, Environment environment) {
        this.namespace = namespace;
        this.scope = scope;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.environment = environment;
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
    public SignatureVisitor visitSuperclass() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.type.superclass((TypeFilled) v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    public SignatureVisitor visitInterface() {
        return this.logAndReturn(new TypeSignatureVisitor(v -> {
            this.builder().child(v);
            if (v.isPresent()) {
                this.type.addInterface((TypeFilled) v.value());
            }
        }, this.scope, this.environment));
    }

    @Override
    protected void finish() {
        if (this.previous != null) {
            this.previousBuilder.build(this.previous);
        }
        this.type.namespace(this.namespace);
        setValue(this.type.build());
        this.builder().andChildren();
    }
}
