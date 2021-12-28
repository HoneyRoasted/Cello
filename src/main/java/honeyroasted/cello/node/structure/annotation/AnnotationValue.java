package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.Node;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.verify.Verification;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AnnotationValue extends Node {

    class Array<T extends AnnotationValue> extends AbstractPropertyHolder implements AnnotationValue {
        private Class<T> type;
        private List<AnnotationValue> values = new ArrayList<>();

        public Array(Class<T> type) {
            this.type = type;
        }

        public Class<T> type() {
            return this.type;
        }

        public Array<T> add(AnnotationValue... values) {
            for (AnnotationValue value : values) {
                this.values.add(value);
            }
            return this;
        }

        public List<AnnotationValue> values() {
            return this.values;
        }

        public Verification<Array<T>> verify() {
            if (this.type.equals(Array.class)) {
                return Verification.builder(this)
                        .invalidAnnotationError("Multidimensional arrays not allowed in annotations")
                        .build();
            }

            Verification.Builder<Array<T>> builder = Verification.builder(this);
            this.values.forEach(a -> {
                if (!this.type.isInstance(a)) {
                    builder.child(
                            Verification.builder(a)
                                    .typeError(Types.type(a.getClass()), Types.type(this.type))
                                    .build());
                } else {
                    builder.child(Verification.success(a));
                }
            });
            return builder.andChildren().build();
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
