package honeyroasted.cello.node.verify;

import honeyroasted.cello.node.ast.CodeNode;
import honeyroasted.javatype.informal.TypeInformal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(error);
    }

    public static <T> Verification<T> success(T val) {
        return Verification.<T>builder().value(val).build();
    }

    public static class Builder<T> {
        private boolean success = true;
        private String message = "Success";
        private T value = null;
        private Throwable error = null;
        private List<Verification<?>> children = new ArrayList<>();
        private ErrorCode errorCode = ErrorCode.UNKNOWN;

        public Verification<T> build() {
            return new Verification<>(this.success, this.message, this.value, this.error, this.children,
                    this.success && this.errorCode == ErrorCode.UNKNOWN ? ErrorCode.NONE : this.errorCode);
        }

        public ErrorCode errorCode() {
            return errorCode;
        }

        public Builder<T> errorCode(ErrorCode errorCode) {
            if (errorCode != ErrorCode.NONE) {
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

        public Builder<T> typeError(TypeInformal left, TypeInformal right) {
            return this.errorCode(ErrorCode.TYPE_ERROR)
                    .message(left.externalName() + " is not assignable to " + right.externalName());
        }

        public Builder<T> varNotFoundError(String name) {
            return this.errorCode(ErrorCode.VAR_NOT_FOUND_ERROR)
                    .message("No variable named '" + name + "' exists in local scope");
        }

        public Builder<T> noChildError() {
            return this.errorCode(ErrorCode.CHILD_FAILED)
                    .message("Child node either failed or had no value");
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
            }
            return this;
        }

    }

    public enum ErrorCode {
        NONE,
        UNKNOWN,
        TYPE_ERROR,
        VAR_NOT_FOUND_ERROR,
        CHILD_FAILED
    }

}
