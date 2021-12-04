package honeyroasted.cello.node.ast.instruction.flow;

import honeyroasted.cello.node.Nodes;
import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.cello.node.ast.TypedNode;
import honeyroasted.cello.properties.AbstractPropertyHolder;

public class ForBlock extends AbstractPropertyHolder implements CodeNode<ForBlock, ScopeBlock> {
    private CodeNode init;
    private TypedNode condition;
    private CodeNode step;

    private CodeNode body;

    public ForBlock(CodeNode init, TypedNode condition, CodeNode step, CodeNode body) {
        this.init = init;
        this.condition = condition;
        this.step = step;
        this.body = body;
    }

    @Override
    public ScopeBlock preprocess() {
        return Nodes.scope(Nodes.sequence(init, new WhileBlock(condition,
                Nodes.sequence(body, step))));
    }
}
