package BTE.configuration.exceptions;

/**
 * Vynimka pri spracovani metakonfiguracie.
 * @author Milan
 */
public class MetaConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MetaConfigurationException(String msg) {
        super(msg);
    }

    public MetaConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
