package BTE.configuration.exceptions;

/**
 * Vynimka pri preklade z modelu do vystupu.
 * @author Milan
 */
public class TranslationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TranslationException(String msg) {
        super(msg);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}

