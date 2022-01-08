package honeyroasted.cello.node.instruction;

import honeyroasted.cello.node.structure.annotation.AnnotationValue;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeFilled;

public interface Nodes {

    static Node defaultValue(TypeFilled type) {
        if (type.equals(Types.BOOLEAN)) {
            return Nodes.constant(false);
        } else if (type.equals(Types.BYTE)) {
            return Nodes.constant((byte) 0);
        } else if (type.equals(Types.CHAR)) {
            return Nodes.constant('\0');
        } else if (type.equals(Types.SHORT)) {
            return Nodes.constant((short) 0);
        } else if (type.equals(Types.INT)) {
            return Nodes.constant(0);
        } else if (type.equals(Types.LONG)) {
            return Nodes.constant(0L);
        } else if (type.equals(Types.FLOAT)) {
            return Nodes.constant(0F);
        } else if (type.equals(Types.DOUBLE)) {
            return Nodes.constant(0D);
        } else {
            return Nodes.constant(null);
        }
    }

    static <K extends AnnotationValue & Node> K constant(Object val) {
        return null;
    }

}
