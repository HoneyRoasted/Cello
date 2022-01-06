package honeyroasted.cello.environment.bytecode.visitor.signature;

import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Type;

import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

public class CelloSignatureVisitor<T extends Type> extends SignatureVisitor {
    private List<CelloSignatureVisitor<?>> visitors = new ArrayList<>();
    private Consumer<Verification<T>> end;

    private Verification.Builder<T> value = Verification.builder();

    public CelloSignatureVisitor(Consumer<Verification<T>> end) {
        super(ASM9);
        this.end = end;
    }

    public CelloSignatureVisitor() {
        this(null);
    }

    protected void finish() {

    }

    public void visitFinish() {
        //SignatureVisitor is very inconsistent with it's visitEnd calls, so we use a custom method
        //and call when a type should be finished
        while (!this.visitors.isEmpty()) {
            this.visitors.remove(0).visitFinish();
        }

        finish();

        if(this.end != null) {
            this.end.accept(this.value.build());
            this.end = null;
        }
        super.visitEnd();
    }

    protected void setValue(T value) {
        this.value.value(value);
    }

    public Verification.Builder<T> builder() {
        return this.value;
    }

    protected  <K extends Type> CelloSignatureVisitor<K> logAndReturn(CelloSignatureVisitor<K> visitor) {
        this.visitors.add(visitor);
        return visitor;
    }

}
