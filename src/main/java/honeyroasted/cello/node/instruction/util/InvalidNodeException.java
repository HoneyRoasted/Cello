package honeyroasted.cello.node.instruction.util;

public class InvalidNodeException extends RuntimeException {

    public InvalidNodeException() {
    }

    public InvalidNodeException(String message) {
        super(message);
    }

    public InvalidNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNodeException(Throwable cause) {
        super(cause);
    }

    public InvalidNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
