package BTE.configuration.communication.interfaces;

/**
 * Rozhranie na vypis. Vyuzivam na vypis varovani.
 * @author Milan
 */
public interface IPrintStream {
    /**
     * Vypise varovanie.
     * @param message
     */
    void println(String message);
}
