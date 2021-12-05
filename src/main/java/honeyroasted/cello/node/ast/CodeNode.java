package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.control.ControlScope;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.LocalScope;
import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.ast.util.UntypedNode;
import honeyroasted.cello.node.verify.Verification;
import org.objectweb.asm.commons.InstructionAdapter;

public interface CodeNode<T extends CodeNode, K extends CodeNode> extends Node {

    default K preprocess() {
        return (K) this;
    }

    default Verification<T> verify(Environment environment, LocalScope localScope, ControlScope controlScope) {
        return Verification.success((T) this);
    }

    default void walkControls(ControlScope controlScope) {

    }

    default void apply(InstructionAdapter adapter, Environment environment, LocalScope localScope, ControlScope controlScope) {
        throw new UnsupportedOperationException("Unprocessed node");
    }

    default K preprocessFully() {
        CodeNode prev = this;
        K val = preprocess();
        while (prev != val) {
            prev = val;
            val = (K) val.preprocess();
        }
        return val;
    }

    default CodeNode<?, ?> untyped() {
        return new UntypedNode(this);
    }

}
