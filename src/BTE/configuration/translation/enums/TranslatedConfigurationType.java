package BTE.configuration.translation.enums;

/**
 * Enumeracny typ urcujuci typ prekladaneho uzla.
 * @author Milan
 */
public enum TranslatedConfigurationType {
    /**
     * Komplexny typ so sekvenciou, a teda aj s potomkami.
     */
    COMPLEX_SEQ,
    /**
     * Prazdny komplexny typ, neobsahuje sekvenciu a teda nema potomkov v strome
     * dokumentu, ma vsak element pre pridavanie atributov.
     */
    COMPLEX_EMPTY,
    /**
     * Komplexny typ so sekvenciou, a teda aj s potomkami. Avsak ma aj
     * atribut mixed s hodnotou true.
     */
    COMPLEX_MIXED,
    /**
     * Jednoduchy typ. Napr. pre obycajny string. Samozrejme moze byt aj s
     * restriction.
     */
    SIMPLE,
    /**
     * Komplexny typ s jednoduchym obsahom. Pravdepodobne bude stacit
     * extension. Ide o pripad jednoducheho typu s atributmi.
     */
    SIMPLE_CONTENT,
    /**
     * Toto bude chciet kusa zamyslenie. Cielovym objektom je element, ktory
     * ma jednoduchy obsah urceny obmedzenim (restriction), ale ma aj atribut/-y.
     * Bez obmedzenia, iba s jednoduchym obsahom postaci SIMPLE_CONTENT.
     * Zda sa ze bude treba pouzit simplecontent extension v kombinacii s
     * restriction - pouzitie zlozeneho typu.
     */
    SIMPLE_CONT_RES
}
