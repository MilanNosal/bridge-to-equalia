package BTE.configuration.exceptions;

/**
 * Vynimka pri chybe prekladu do modelu.
 * @author Milan
 */
public class ParsingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ParsingException(String msg) {
        super(msg);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
