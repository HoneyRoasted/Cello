package honeyroasted.cello.verify;

import honeyroasted.javatype.Type;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Verification<T> {
    private Verify.Level level;
    private T value;
    private String message;

    private Object source;

    private Verify.Code code;
    private Throwable exception;

    private List<Verification<?>> children;

    public Verification(Verify.Level level, T value, String message, Object source, Verify.Code code, Throwable exception, List<Verification<?>> children) {
        this.level = level;
        this.value = value;
        this.message = message == null ? level.toString().toLowerCase() : message;
        this.source = source;
        this.code = code;
        this.exception = exception;
        this.children = children;
    }

    public static <K> VerificationBuilder<K> builder() {
        return new VerificationBuilder<>();
    }

    public static <K> VerificationBuilder<K> builder(K value) {
        return Verification.<K>builder().value(value);
    }

    public static <K> Verification<K> success(K value) {
        return builder(value).build();
    }

    public static <K> Verification<K> success(Object source, K value) {
        return builder(value).source(source).build();
    }

    public static <K> Verification<K> error(Verify.Code code, String message, Object... format) {
        return Verification.<K>builder().error(code, message, format).build();
    }

    public static <K> Verification<K> error(Verify.Code code, String message, Throwable ex, Object... format) {
        return Verification.<K>builder().error(code, message, ex, format).build();
    }

    public static <K> Verification<K> error(Object source, Verify.Code code, String message, Object... format) {
        return Verification.<K>builder().error(code, message, format).source(source).build();
    }

    public static <K> Verification<K> error(Object source, Verify.Code code, String message, Throwable ex, Object... format) {
        return Verification.<K>builder().source(source).error(code, message, ex, format).build();
    }

    public <K> Verification<K> map(Function<T, K> function) {
        if (this.value != null) {
            return (Verification<K>) this.toBuilder().value((T) function.apply(this.value)).build();
        } else {
            return (Verification<K>) this;
        }
    }

    public T orElse(T value) {
        return success() ? value().orElse(value) : value;
    }

    public VerificationBuilder<T> toBuilder() {
        return Verification.<T>builder().from(this);
    }

    public String format(Verify.Level level, boolean unicode) {
        return VerificationFormatter.format(this, unicode, level);
    }

    public String format(Verify.Level level) {
        return format(level, false);
    }

    public String format() {
        return format(Verify.Level.WARNING);
    }

    @Override
    public String toString() {
        return "Verification{" +
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
