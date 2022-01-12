package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.javatype.Types;

import java.util.List;

public class Switch {
    //TODO

    @Child
    private Node value;
    private List<CaseBlock> cases;

    public Switch(Node value, List<CaseBlock> cases) {
        this.value = value;
        this.cases = cases;
    }

    public static class CaseBlock {
        private Node condition;
        private Node body;

        public CaseBlock(Node condition, Node body) {
            this.condition = Nodes.convert(condition, Types.BOOLEAN);
            this.body = body;
        }

        public Node condition() {
            return condition;
        }

        public Node body() {
            return body;
        }
    }

}
