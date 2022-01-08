package honeyroasted.cello.node.instruction;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.util.SimpleNode;
import honeyroasted.cello.properties.PropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public interface Node extends PropertyHolder {

    Verification<TypeInformal> verify(Environment environment, CodeContext context);

    void apply(InstructionAdapter adapter, Environment environment, CodeContext context);

    Verification<TypeInformal> verification();

    TypeInformal type();

    default boolean success() {
        return this.verification().success();
    }

    default boolean success(Verify.Level level) {
        return this.verification().success(level);
    }

    default Node toUntyped() {
        return of((e, c) -> verify(e, c).map(k -> Types.VOID), (a, e, c) -> {
            apply(a, e, c);
            int size = TypeUtil.size(this.type());

            if (size % 2 != 0) {
                a.dup();
            }

            for (int i = 0; i < size / 2; i++) {
                a.dup2();
            }
        });
    }

    static Node of(BiFunction<Environment, CodeContext, Verification<TypeInformal>> verify, TriConsumer<InstructionAdapter, Environment, CodeContext> apply) {
        return new SimpleNode(verify, apply);
    }

}
