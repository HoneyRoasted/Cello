package honeyroasted.cello.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VerificationBuilder<T> {
    private Verify.Level level = Verify.Level.SUCCESS;
    private T value = null;
    private String message = null;

    private Object source = null;

    private Verify.Code code = Verify.Code.SUCCESS;
    private Throwable exception = null;

    private List<Verification<?>> children = new ArrayList<>();

    public Verification<T> build() {
        return new Verification<>(this.level, this.value, this.message, this.source, this.code, this.exception, this.children);
    }

    public VerificationBuilder<T> from(Verification<T> verification) {
        this.level = verification.level();
        this.value = verification.value().orElse(null);
        this.message = verification.message();
        this.source = verification.source().orElse(null);
        this.code = verification.code();
        this.exception = verification.exception().orElse(null);
        this.children = new ArrayList<>(verification.children());
        return this;
    }

    public VerificationBuilder<T> andChildren() {
        boolean succ = this.children.stream().allMatch(v -> v.success());
        if (!succ) {
            this.error(Verify.Code.CHILD_FAILED_ERROR, "One or more children failed");
        } else {
            this.level(Verify.Level.SUCCESS);
            this.code(Verify.Code.SUCCESS);
            this.message("All children passed");
        }
        return this;
    }

    public VerificationBuilder<T> orChildren() {
        boolean succ = this.children.stream().anyMatch(v -> v.success());
        if (!succ) {
            this.error(Verify.Code.CHILD_FAILED_ERROR, "All children failed");
        } else {
            this.level(Verify.Level.SUCCESS);
            this.code(Verify.Code.SUCCESS);
            this.message("One or more children passed");
        }
        return this;
    }

    public VerificationBuilder<T> error(Verify.Code code, String message, Object... format) {
        return level(Verify.Level.ERROR)
                .message(String.format(message, format))
                .code(code);
    }

    public VerificationBuilder<T> error(Verify.Code code, String message, Throwable ex, Object... format) {
        return level(Verify.Level.ERROR)
                .message(String.format(message, format))
                .code(code)
                .exception(ex);
    }

    public VerificationBuilder<T> level(Verify.Level level) {
        this.level = level;
        if (level == Verify.Level.ERROR && this.code == Verify.Code.SUCCESS) {
            this.code = Verify.Code.UNKNOWN_ERROR;
        } else if (level == Verify.Level.SUCCESS) {
            this.code = Verify.Code.SUCCESS;
        }
        return this;
    }

    public VerificationBuilder<T> value(T value) {
        this.value = value;
        return this;
    }

    public VerificationBuilder<T> message(String message) {
        this.message = message;
        return this;
    }

    public VerificationBuilder<T> message(String message, Object... format) {
        return this.message(String.format(message, format));
    }

    public VerificationBuilder<T> source(Object source) {
        this.source = source;
        return this;
    }

    public VerificationBuilder<T> code(Verify.Code code) {
        this.code = code;
        return this;
    }

    public VerificationBuilder<T> exception(Throwable ex) {
        this.exception = ex;
        return this;
    }

    public VerificationBuilder<T> children(Verification<?>... children) {
        Collections.addAll(this.children, children);
        return this;
    }

    public VerificationBuilder<T> children(Collection<Verification<?>> children) {
        this.children.addAll(children);
        return this;
    }

    public VerificationBuilder<T> child(Verification<?> child) {
        this.children.add(child);
        return this;
    }

    public VerificationBuilder<T> clearChildren() {
        this.children.clear();
        return this;
    }

    @Override
    public String toString() {
        return "VerificationBuilder{" +
                "level=" + level +
                ", value=" + value +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}';
    }

    public boolean success(Verify.Level level) {
        return this.level.weight() <= level.weight();
    }

    public boolean success() {
        return success(Verify.Level.WARNING);
    }

    public Verify.Level level() {
        return this.level;
    }

    public Optional<T> value() {
        return Optional.ofNullable(this.value);
    }

    public String message() {
        return this.message;
    }

    public Optional<Object> source() {
        return Optional.ofNullable(this.source);
    }

    public Verify.Code code() {
        return this.code;
    }

    public Optional<Throwable> exception() {
        return Optional.ofNullable(this.exception);
    }

    public List<Verification<?>> children() {
        return this.children;
    }
}
