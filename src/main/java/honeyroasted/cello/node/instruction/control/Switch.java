package honeyroasted.cello.node.instruction.control;

import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.util.Child;

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

    }

}
