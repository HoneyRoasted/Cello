package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.node.modifier.AbstractModifiable;
import honeyroasted.cello.properties.PropertyHolder;

import java.util.ArrayList;
import java.util.List;

public class AbstractAnnotated extends AbstractModifiable implements Annotated, PropertyHolder {
    private List<AnnotationNode> annotations = new ArrayList<>();

    @Override
    public List<AnnotationNode> annotations() {
        return this.annotations;
    }

}
