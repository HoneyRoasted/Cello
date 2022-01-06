package honeyroasted.cello.node.structure;

import honeyroasted.cello.node.instruction.TypedNode;
import honeyroasted.cello.node.structure.annotation.AbstractAnnotated;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.Optional;

public class FieldNode extends AbstractAnnotated {
    private String name;
    private TypeInformal type;
    private TypedNode value;
    private ClassNode owner;

    public FieldNode(String name, ClassNode owner, TypeInformal type, TypedNode value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.owner = owner;
    }

    public FieldNode(String name, ClassNode owner, TypeFilled type) {
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

    public Optional<TypedNode> value() {
        return Optional.ofNullable(this.value);
    }

    public FieldNode setValue(TypedNode value) {
        this.value = value;
        return this;
    }

    public FieldNode setType(TypeInformal type) {
        this.type = type;
        return this;
    }
}
