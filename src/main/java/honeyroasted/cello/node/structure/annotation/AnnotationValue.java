package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.node.Node;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;

import java.util.ArrayList;
import java.util.List;

public interface AnnotationValue extends Node {

    class Array extends AbstractPropertyHolder implements AnnotationValue {
        private List<AnnotationValue> values = new ArrayList<>();

        public Array add(AnnotationValue... values) {
            for (AnnotationValue value : values) {
                this.values.add(value);
            }
            return this;
        }

        public List<AnnotationValue> values() {
            return this.values;
        }
    }

    class Enum extends AbstractPropertyHolder implements AnnotationValue {
        private Namespace enumClass;
        private String name;

        public Enum(Namespace enumClass, String name) {
            this.enumClass = enumClass;
            this.name = name;
        }

        public Namespace enumClass() {
            return this.enumClass;
        }

        public String name() {
            return this.name;
        }
    }

}
