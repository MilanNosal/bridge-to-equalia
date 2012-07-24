package BTE.configuration.model.metamodel.enums;

/**
 * Typ elementu, ktory posluzi ako mapovany zdroj informacie. Moze to byt napr.
 * anotacia, deklarovana vlastnost anotacie a pod.
 * @author Milan
 */
public enum SourceType {
    /**
     * Zdroj informacie je anotacia, zastresena anotacnym typom.
     */
    ANNOTATION,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou primitivneho typu.
     */
    DECL_PROP_PRIMITIVE,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou anotacneho typu.
     */
    DECL_PROP_ANNOTATION,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou pola primitivneho typu.
     */
    DECL_PROP_ARRAY_PRIMITIVE,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou pola retazcov java.lang.String.
     */
    DECL_PROP_ARRAY_STRING,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou pola enumeracneho typu.
     */
    DECL_PROP_ARRAY_ENUM,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou pola anotacneho typu.
     */
    DECL_PROP_ARRAY_ANNOTATION,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou enumeracneho typu.
     */
    DECL_PROP_ENUM,

    /**
     * Zdroj informacie je deklarovana vlastnost nejakej anotacie s navratovou
     * hodnotou retazca - java.lang.String.
     */
    DECL_PROP_STRING,

    /**
     * Definovanie vlastneho sposobu tahania informacie. Tento typ indikuje,
     * ze pre tahanie informacie sa ma pouzit implementacia InformationExtractor,
     * ktoru doda pouzivatel.
     */
    USER_DEFINED,

    /**
     * Zdroj informacie nie je ziadnym elementom zdrojovych kodov. Teda neexistuje
     * mapovanie na zdrojove kody, vyznamne pri wrapperoch.
     */
    NONE
    
}
