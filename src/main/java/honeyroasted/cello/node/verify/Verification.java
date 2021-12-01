package honeyroasted.cello.node.verify;

import honeyroasted.cello.node.ast.CodeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Verification<T> {
    private boolean success;
    private String message;
    private T value;
    private Throwable error;

    private List<Verification<?>> children;

    public Verification(boolean success, String message, T value, Throwable error, List<Verification<?>> children) {
        this.success = success;
        this.message = message;
        this.value = value;
        this.error = error;
        this.children = children;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
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
        return new Verification<>(true, "Success", val, null, Collections.emptyList());
    }

    public static <T> Verification<T> failure(String message) {
        return failure(message, null);
    }

    public static <T> Verification<T> failure(String message, Throwable throwable) {
        return new Verification<>(false, message, null, throwable, Collections.emptyList());
    }

    public static class Builder<T> {
        private boolean success = true;
        private String message = "Success";
        private T value = null;
        private Throwable error = null;
        private List<Verification<?>> children = new ArrayList<>();

        public Verification<T> build() {
            return new Verification<>(this.success, this.message, this.value, this.error, this.children);
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

}
