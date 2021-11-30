package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.Scope;
import honeyroasted.cello.node.Node;
import org.objectweb.asm.commons.InstructionAdapter;

public interface CodeNode extends Node {

    void apply(InstructionAdapter adapter, Environment environment, Scope scope);

}
