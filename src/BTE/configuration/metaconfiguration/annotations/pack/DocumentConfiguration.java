package BTE.configuration.metaconfiguration.annotations.pack;

/**
 * Metakonfiguracia tykajuca sa generovaneho vystupneho dokumentu a vstupnych
 * dokumentov.
 * @author Milan
 */
public @interface DocumentConfiguration {
    /**
     * Nazov korenoveho elementu.
     * @return
     */
    String rootName();

    /**
     * Nazov menneho priestoru dokumentov.
     * @return
     */
    String XMLNameSpace();

    /**
     * Informacia o mapovani generickych nazvov jazykovych elementov.
     * @return
     */
    ElementKinds elementKinds();
}
