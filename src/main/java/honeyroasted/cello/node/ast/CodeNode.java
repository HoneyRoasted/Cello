package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.ast.util.UntypedNode;
import honeyroasted.cello.node.verify.Verification;
import org.objectweb.asm.commons.InstructionAdapter;

public interface CodeNode<T extends CodeNode> extends Node {

    default void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope) {
        throw new UnsupportedOperationException("Unprocessed node");
    }

    default Verification<T> verify(Environment environment, LocalScope localScope) {
        return Verification.success((T) this);
    }

    default Verification<T> preprocess() {
        return Verification.success((T) this);
    }

    default CodeNode<?> untyped() {
        return new UntypedNode(this);
    }

}
