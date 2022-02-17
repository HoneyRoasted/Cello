package honeyroasted.cello.node.instruction;

import honeyroasted.cello.TriConsumer;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.util.SimpleNode;
import honeyroasted.cello.node.instruction.util.UntypedNode;
import honeyroasted.cello.properties.PropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.function.BiFunction;

public interface Node extends PropertyHolder {

    void setExpected(TypeInformal type);

    TypeInformal expected();

    Verification<TypeInformal> verify(Environment environment, CodeContext context);

    void apply(InstructionAdapter adapter, Environment environment, CodeContext context);

    Verification<TypeInformal> verification();

    TypeInformal type();

    default boolean terminal() {
        return false;
    }

    default boolean success() {
        return this.verification().success();
    }

    default boolean success(Verify.Level level) {
        return this.verification().success(level);
    }

    default Node toUntyped() {
        return new UntypedNode(this);
    }

    static Node of(BiFunction<Environment, CodeContext, Verification<TypeInformal>> verify, TriConsumer<InstructionAdapter, Environment, CodeContext> apply) {
        return new SimpleNode(verify, apply);
    }

}
