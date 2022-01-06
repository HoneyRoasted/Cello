package honeyroasted.cello.environment.bytecode.signature;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.informal.TypeWild;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

public class TypeSignatureVisitor extends CelloSignatureVisitor<TypeInformal> {
    private TypeVarScope scope;
    private Environment environment;

    private TypeFilled.Builder filled = null;
    private TypeWild.Builder wild = null;

    private TypeInformal type = null;

    public TypeSignatureVisitor(Consumer<Verification<TypeInformal>> end, TypeVarScope scope, Environment environment) {
        super(end);
        this.scope = scope;
        this.environment = environment;
    }

    public TypeSignatureVisitor(TypeVarScope scope, Environment environment) {
        this.scope = scope;
        this.environment = environment;
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
        return logAndReturn(new TypeSignatureVisitor(f -> {
            this.builder().child(f);
            if (f.isPresent()) {
                this.type = f.value().array(1);
            }
        }, this.scope, this.environment));
    }

    @Override
    public void visitClassType(String name) {
        Namespace namespace = Namespace.internal(name);
        Verification<ClassNode> node = this.environment.lookup(namespace);
        this.builder().child(node);

        if (node.isPresent()) {
            this.filled = Types.filled().type(node.value().type());
        }
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
                    this.builder().child(f);

                    if (f.isPresent()) {
                        TypeWild wild;
                        if (wildcard == '-') {
                            wild = Types.wild().lower(f.value()).build();
                        } else {
                            wild = Types.wild().upper(f.value()).build();
                        }
                        this.filled.addGeneric(wild);
                    }
                }, this.scope, this.environment));
            } else {
                return logAndReturn(new TypeSignatureVisitor(f -> {
                    this.builder().child(f);

                    if (f.isPresent()) {
                        this.filled.addGeneric(f.value());
                    }
                }, this.scope, this.environment));
            }
        }
        return super.visitTypeArgument(wildcard);
    }

    @Override
    public void finish() {
        if (this.builder().value() == null) {
            if (this.filled != null) {
                this.setValue(this.filled.build());
            } else if (this.wild != null) {
                this.setValue(this.wild.build());
            }
        }

        if (this.builder().success()) {
            this.builder().andChildren();
        }
    }
}
