package BTE.configuration.model.metamodel.enums;

/**
 * Enumeracny typ, urcujuci, ako ma byt informacia spracovana v XML
 * dokumente.
 * @author Milan
 */
public enum XMLProcessing {
    /**
     * Vysledna zlozka XML dokumentu ma byt element.
     */
    ELEMENT,
    /**
     * Vysledna zlozka dokumentu ma byt atribut elementu.
     */
    ATTRIBUTE,
    /**
     * Informacia oznacena tymto typom nema byt spracovana.
     * Potomkovia tejto informacie sa pri generovani priradia informacii nad nou
     * (ak aj ta ma byt preskocena tak este vyssie).
     */
    SKIP_PROCESS
}
