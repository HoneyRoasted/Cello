package honeyroasted.cello.verify;

import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.Type;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class Verification<T> {
    private boolean success;
    private String message;
    private T value;
    private Throwable error;
    private ErrorCode errorCode;

    private List<Verification<?>> children;

    public Verification(boolean success, String message, T value, Throwable error, List<Verification<?>> children, ErrorCode errorCode) {
        this.success = success;
        this.message = message;
        this.value = value;
        this.error = error;
        this.children = children;
        this.errorCode = errorCode;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(T value) {
        Builder<T> b = builder();
        b.value(value);
        return b;
    }

    public ErrorCode errorCode() {
        return this.errorCode;
    }

    public List<Verification<?>> children() {
        return this.children;
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public T value() {
        if (this.value == null) {
            throw new NoSuchElementException();
        }
        return this.value;
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(error);
    }

    public static <T> Verification<T> success(T val) {
        return Verification.<T>builder().value(val).build();
    }

    public <K> Verification<K> map(Function<T, K> function) {
        if (this.success()) {
            K value = function.apply(this.value);
            if (value == null) {
                return Verification.<K>builder()
                        .child(this)
                        .failedMappingError()
                        .build();
            } else {
                return Verification.<K>builder().from((Verification<K>) this)
                        .value(value)
                        .build();
            }
        } else {
            return (Verification<K>) this;
        }
    }

    public T orElse(T value) {
        return this.isPresent() ? this.value() : value;
    }

    public boolean isPresent() {
        return this.success() && this.value != null;
    }

    public String format() {
        return VerificationFormatter.format(this);
    }

    public static class Builder<T> {
        private boolean success = true;
        private String message = "Success";
        private T value = null;
        private Throwable error = null;
        private List<Verification<?>> children = new ArrayList<>();
        private ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;

        public Builder<T> from(Verification<T> verification) {
            this.success = verification.success;
            this.message = verification.message;
            this.value = verification.value;
            this.error = verification.error;
            this.children = new ArrayList<>(verification.children);
            this.errorCode = verification.errorCode;
            return this;
        }

        public Verification<T> build() {
            return new Verification<>(this.success, this.message, this.value, this.error, this.children,
                    this.success && this.errorCode == ErrorCode.UNKNOWN_ERROR ? ErrorCode.SUCCESS : this.errorCode);
        }

        public ErrorCode errorCode() {
            return errorCode;
        }

        public Builder<T> errorCode(ErrorCode errorCode) {
            if (errorCode != ErrorCode.SUCCESS) {
                this.success(false);
            }
            this.errorCode = errorCode;
            return this;
        }

        public boolean success() {
            return this.success;
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public String message() {
            return this.message;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public T value() {
            return this.value;
        }

        public Builder<T> value(T value) {
            this.value = value;
            return this;
        }

        public Throwable error() {
            return this.error;
        }

        public Builder<T> error(Throwable error) {
            this.error = error;
            return this;
        }

        public List<Verification<?>> children() {
            return this.children;
        }

        public Builder<T> children(List<Verification<?>> children) {
            this.children = children;
            return this;
        }

        public Builder<T> child(Verification<?> child) {
            this.children.add(child);
            return this;
        }

        public Builder<T> andChildren() {
            boolean suck = this.children.stream().allMatch(v -> v.success());
            if (suck) {
                this.success = true;
                this.message = "All children passed";
            } else {
                this.success = false;
                this.message = "At least one child failed";
                this.errorCode = ErrorCode.CHILD_FAILED_ERROR;
            }
            return this;
        }

        public Builder<T> orChildren() {
            boolean suck = this.children.stream().anyMatch(v -> v.success());
            if (suck) {
                this.success = true;
                this.message = "At least one child passed";
            } else {
                this.success = false;
                this.message = "All children failed";
                this.errorCode = ErrorCode.CHILD_FAILED_ERROR;
            }
            return this;
        }

        public Builder<T> varAlreadyDefinedError(String name) {
            return this.errorCode(ErrorCode.VAR_ALREADY_DEFINED_ERROR)
                    .message("Variable '" + name + "' has already been defined");
        }

        public Builder<T> invalidTypeError(TypeInformal type) {
            return this.errorCode(ErrorCode.INVALID_TYPE_ERROR)
                    .message("Invalid type '" + (type == null ? "null" : type.externalName()) + "'");
        }

        public Builder<T> typeError(TypeInformal left, TypeInformal right) {
            return this.errorCode(ErrorCode.TYPE_ERROR)
                    .message(left.externalName() + " is not assignable to " + right.externalName());
        }

        public Builder<T> varNotFoundError(String name) {
            return this.errorCode(ErrorCode.VAR_NOT_FOUND_ERROR)
                    .message("No variable named '" + name + "' exists in local scope");
        }

        public Builder<T> noChildError() {
            return this.errorCode(ErrorCode.CHILD_FAILED_ERROR)
                    .message("Child node either failed or had no value");
        }

        public Builder<T> invalidConstant(Class<?> aClass) {
            return this.errorCode(ErrorCode.INVALID_CONSTANT_ERROR)
                    .message("Invalid constant type " + aClass.getName());
        }

        public Builder<T> controlError(String name, String kind) {
            return this.errorCode(ErrorCode.CONTROL_FLOW_ERROR)
                    .message(kind + " statement not allowed here" + (name == null ? "" : " no block called '" + name + "' found"));
        }

        public Builder<T> illegalCastError(TypeInformal type, TypeInformal target) {
            return this.errorCode(ErrorCode.ILLEGAL_CAST_ERROR)
                    .message("Inconvertible types, cannot cast " + type.externalName() + " to " + target.externalName());
        }

        public Builder<T> invalidAnnotationError(String message) {
            return this.errorCode(ErrorCode.INVALID_ANNOTATION_ERROR)
                    .message(message);
        }

        public Builder<T> typeNotFoundError(Type type) {
            return this.errorCode(ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Could not resolve " + type.externalName());
        }

        public Builder<T> typeNotFoundError(Namespace namespace) {
            return this.errorCode(ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Could not resolve " + namespace.name());
        }

        public Builder<T> typeVarNotFoundError(T name) {
            return this.errorCode(ErrorCode.DUPLICATE_TYPE_VAR)
                    .message("Type variable '" + name + "' already defined");
        }

        public Builder<T> failedMappingError() {
            return this.errorCode(ErrorCode.FAILED_MAPPING)
                    .message("Failed to map to new value internally");
        }

        public Builder<T> thisNotAvailable() {
            return this.errorCode(ErrorCode.THIS_NOT_AVAILABLE_ERROR)
                    .message("'this' not available in static context");
        }
    }

    public enum ErrorCode {
        SUCCESS,
        UNKNOWN_ERROR,
        TYPE_ERROR,
        VAR_NOT_FOUND_ERROR,
        VAR_ALREADY_DEFINED_ERROR,
        CHILD_FAILED_ERROR,
        INVALID_TYPE_ERROR,
        INVALID_CONSTANT_ERROR,
        CONTROL_FLOW_ERROR,
        ILLEGAL_CAST_ERROR,
        INVALID_ANNOTATION_ERROR,
        TYPE_NOT_FOUND_ERROR,
        DUPLICATE_TYPE_VAR,
        THIS_NOT_AVAILABLE_ERROR,

        FAILED_MAPPING
    }

}
