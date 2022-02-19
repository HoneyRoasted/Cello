package honeyroasted.cello.node.structure.annotation;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.FieldNode;
import honeyroasted.cello.properties.PropertyHolder;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.AnnotationVisitor;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface AnnotationValue extends PropertyHolder {

    static void apply(String name, AnnotationValue value, AnnotationVisitor visitor) {

    }

    class Array extends AbstractPropertyHolder implements AnnotationValue {
        private List<AnnotationValue> values = new ArrayList<>();

        public Array add(AnnotationValue... values) {
            Collections.addAll(this.values, values);
            return this;
        }

        public List<AnnotationValue> values() {
            return this.values;
        }

        public void apply(AnnotationVisitor visitor) {
            this.values.forEach(val -> AnnotationValue.apply(null, val, visitor));
        }
    }

    class Enum extends AbstractPropertyHolder implements AnnotationValue {
        private TypeParameterized enumClass;
        private String name;

        public Enum(TypeParameterized enumClass, String name) {
            this.enumClass = enumClass;
            this.name = name;
        }

        public TypeParameterized enumClass() {
            return this.enumClass;
        }

        public String name() {
            return this.name;
        }

        public Verification<TypeInformal> verify(Environment environment) {
            VerificationBuilder<TypeInformal> builder = new VerificationBuilder<>();

            Verification<ClassNode> cls = environment.lookup(this.enumClass);
            builder.child(cls);
            if (cls.success() && cls.value().isPresent()) {
                ClassNode node = cls.value().get();
                Optional<FieldNode> field = node.fields().stream().filter(f -> f.name().equals(this.name)).findFirst();
                if (field.isPresent()) {
                    FieldNode fn = field.get();
                    if (!fn.modifiers().has(Modifier.ENUM)) {
                        builder.child(Verification.error(this, Verify.Code.VAR_NOT_FOUND_ERROR, "Field '%s#%s' is not an enum value", this.enumClass, this.name));
                    }
                } else {
                    builder.child(Verification.error(this, Verify.Code.VAR_NOT_FOUND_ERROR, "Field '%s#%s' not found", this.enumClass, this.name));
                }
            }

            return builder.andChildren().build();
        }

        public TypeInformal type() {
            return this.enumClass.withArguments();
        }

    }

}
