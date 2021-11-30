package honeyroasted.cello.node.verify;

import java.util.Optional;

public class Verification<T> {
    private boolean success;
    private String message;
    private T value;
    private Throwable error;

    public Verification(boolean success, String message, T value, Throwable error) {
        this.success = success;
        this.message = message;
        this.value = value;
        this.error = error;
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
        return new Verification<>(true, "Success", val, null);
    }

    public static <T> Verification<T> failure(String message) {
        return failure(message, null);
    }

    public static <T> Verification<T> failure(String message, Throwable throwable) {
        return new Verification<>(false, message, null, throwable);
    }

}
