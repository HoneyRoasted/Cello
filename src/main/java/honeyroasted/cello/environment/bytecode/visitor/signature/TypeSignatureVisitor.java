package honeyroasted.cello.environment.bytecode.visitor.signature;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.ParameterizedNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.informal.TypeWild;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

public class TypeSignatureVisitor extends CelloSignatureVisitor<TypeInformal> {
    private ParameterizedNode scope;
    private Environment environment;

    private TypeFilled.Builder filled = null;
    private TypeWild.Builder wild = null;

    private TypeInformal type = null;

    public TypeSignatureVisitor(Consumer<Verification<TypeInformal>> end, ParameterizedNode scope, Environment environment) {
        super(end);
        this.scope = scope;
        this.environment = environment;
    }

    public TypeSignatureVisitor(ParameterizedNode scope, Environment environment) {
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
        this.type = Types.ref(this.scope.fetchOrPutTypeVar(name));
        this.setValue(this.type);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return logAndReturn(new TypeSignatureVisitor(f -> {
            this.builder().child(f);
            if (f.success() && f.value().isPresent()) {
                this.type = f.value().get().array(1);
            }
        }, this.scope, this.environment));
    }

    @Override
    public void visitClassType(String name) {
        Namespace namespace = Namespace.internal(name);
        Verification<ClassNode> node = this.environment.lookup(namespace);
        this.builder().child(node);

        if (node.success() && node.value().isPresent()) {
            this.filled = Types.filled().type(node.value().get().parameterizedType());
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

                    if (f.success() && f.value().isPresent()) {
                        TypeWild wild;
                        if (wildcard == '-') {
                            wild = Types.wild().lower(f.value().get()).build();
                        } else {
                            wild = Types.wild().upper(f.value().get()).build();
                        }
                        this.filled.addGeneric(wild);
                    }
                }, this.scope, this.environment));
            } else {
                return logAndReturn(new TypeSignatureVisitor(f -> {
                    this.builder().child(f);

                    if (f.success() && f.value().isPresent()) {
                        this.filled.addGeneric(f.value().get());
                    }
                }, this.scope, this.environment));
            }
        }
        return super.visitTypeArgument(wildcard);
    }

    @Override
    public void finish() {
        if (this.builder().value().isEmpty()) {
            if (this.filled != null) {
                this.setValue(this.filled.build());
            } else if (this.wild != null) {
                this.setValue(this.wild.build());
            } else {
                this.setValue(Types.OBJECT);
            }
        }

        if (this.builder().success()) {
            this.builder().andChildren();
        }
    }
}
