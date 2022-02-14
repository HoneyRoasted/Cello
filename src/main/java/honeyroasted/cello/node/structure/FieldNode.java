package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.Optional;

public class FieldNode extends AbstractAnnotated {
    private String name;
    private TypeInformal type;
    private ClassNode owner;

    private Node value;

    public FieldNode(String name, ClassNode owner, TypeInformal type, Node value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.owner = owner;
    }

    public FieldNode(String name, ClassNode owner, TypeInformal type) {
        this(name, owner, type, null);
    }

    public ClassNode owner() {
        return this.owner;
    }

    public String name() {
        return this.name;
    }

    public TypeInformal type() {
        return this.type;
    }

    public Optional<Node> value() {
        return Optional.ofNullable(this.value);
    }

    public FieldNode setName(String name) {
        this.name = name;
        return this;
    }

    public FieldNode setOwner(ClassNode owner) {
        this.owner = owner;
        return this;
    }

    public FieldNode setValue(Node value) {
        this.value = value;
        return this;
    }

    public FieldNode setType(TypeInformal type) {
        this.type = type;
        return this;
    }
}
