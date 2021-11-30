package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.Scope;
import honeyroasted.cello.node.Child;
import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.verify.Verification;
import org.objectweb.asm.commons.InstructionAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.Stack;

public interface CodeNode extends Node {

    default void apply(InstructionAdapter adapter, Environment environment, Scope scope) {
        throw new UnsupportedOperationException("Unprocessed node");
    }

    default Verification<CodeNode> typeCheck(Environment environment, Scope scope) {
        return Verification.success(this);
    }

    default Verification<CodeNode> preprocess() {
        return Verification.success(this);
    }

}
