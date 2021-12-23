package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationNode extends AbstractPropertyHolder implements AnnotationValue {
    private TypeParameterized annotation;
    private Map<String, AnnotationValue> values = new LinkedHashMap<>();

    public AnnotationNode(TypeParameterized annotation) {
        this.annotation = annotation;
    }

    public Map<String, AnnotationValue> values() {
        return this.values;
    }

    public AnnotationNode put(String key, AnnotationValue value) {
        this.values.put(key, value);
        return this;
    }

}
