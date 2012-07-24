package BTE.configuration.model.metamodel.enums;

/**
 * Typ obsahu polozky v XSD.
 * @author Milan
 */
public enum TypeOfElement {
    /**
     * Obsahuje obsah definovany enumeracnym typom.
     */
    ENUMERATED_VALUE,
    /**
     * Moze mat aj hodnotu.
     */
    VALUE,
    /**
     * Nema ziadnu hodnotu - teda bez hodnoty.
     */
    NONE
}
