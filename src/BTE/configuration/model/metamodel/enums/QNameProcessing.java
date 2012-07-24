package BTE.configuration.model.metamodel.enums;

/**
 * Enumeracny typ urcujuci ako ma byt mapovane meno cieloveho jazykoveho elementu.
 * @author Milan
 */
public enum QNameProcessing {
    /**
     * Vypisat cele meno pri danej konfiguracnej informacii.
     * V pripade metody ide o cely nazov bez modifikatorov a throws klauzuly.
     */
    FULL_PRINT,
    /**
     * Vypisat len cast, ktora presne urci cielovy jazykovy element informacie.
     * Cele meno sa da zlozit pomocou kontextu rodica.
     */
    CONTEXT_PRINT,
    /**
     * Vypis iba jednoducheho mena elementu.
     */
    SIMPLE_PRINT
}
