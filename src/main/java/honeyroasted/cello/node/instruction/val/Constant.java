package honeyroasted.cello.node.instruction.val;

import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.structure.annotation.AnnotationValue;

public interface Constant<T> extends Node, AnnotationValue {

    T value();

}
