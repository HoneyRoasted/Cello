package honeyroasted.cello.verify;

public class VerificationException extends RuntimeException {
    private Verification<?> verification;

    public VerificationException(Verification<?> verification) {
        this.verification = verification;
    }

    public VerificationException(String message, Verification<?> verification) {
        super(message);
        this.verification = verification;
    }

    public VerificationException(String message, Throwable cause, Verification<?> verification) {
        super(message, cause);
        this.verification = verification;
    }

    public VerificationException(Throwable cause, Verification<?> verification) {
        super(cause);
        this.verification = verification;
    }

    public VerificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Verification<?> verification) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.verification = verification;
    }
}
