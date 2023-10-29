package skjsjhb.rhytick.opfw.je.cherry;

/**
 * Exceptions related to Cherry APIs.
 */
public class CherryRuntimeException extends RuntimeException {
    public CherryRuntimeException(String message) {
        super(message);
    }

    public CherryRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
