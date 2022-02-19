package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.parameterized.TypeParameterized;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AnnotationNode extends AbstractPropertyHolder implements AnnotationValue {
    private TypeParameterized annotation;
    private Map<String, AnnotationValue> values = new LinkedHashMap<>();

    public AnnotationNode(TypeParameterized annotation) {
        this.annotation = annotation;
    }

    public Map<String, AnnotationValue> values() {
        return this.values;
    }

    public TypeParameterized annotation() {
        return annotation;
    }

    public AnnotationNode put(String key, AnnotationValue value) {
        this.values.put(key, value);
        return this;
    }

    public boolean isVisible(Environment environment) {
        Verification<ClassNode> lookup = environment.lookup(this.annotation);
        if (lookup.success() && lookup.value().isPresent()) {
            ClassNode node = lookup.value().get();
            Optional<AnnotationNode> target = node.annotations().stream().filter(n ->
                    n.annotation().namespace().equals(Namespace.of(Retention.class))).findFirst();
            if (target.isPresent()) {
                AnnotationNode retention = target.get();
                return retention.values().containsKey("value")
                        && retention.values().get("value") instanceof AnnotationValue.Enum elem
                        && elem.enumClass().equals(Namespace.of(RetentionPolicy.class))
                        && elem.name().equals(RetentionPolicy.RUNTIME.name());
            }
        }

        return false;
    }

}
