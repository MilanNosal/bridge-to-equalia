package BTE.configuration.model.metamodel.enums;

/**
 * Enumeracny typ, ktory sa urcuje politiku prehladavania modelu zdrojovych
 * kodov pri urcovani suvislosti medzi dvoma roznymi anotaciami.
 * @author Milan
 */
public enum RelativePositionToAnchor {
    /**
     * Pozicia dcerskeho elementu na rovnakej urovni zdrojovych kodov. Tj. napr. na tej istej
     * triede. Principialne to znamena, ze je na tom istom cielovom jazykovom
     * elemente.
     */
    SAME_LVL,
    /**
     * Rovnake ako pri SAME_LVL, ibaze hladat sa moze aj na nizsej
     * (konkretnejsej) urovni. Tj. smerom od balika k clenskym premennym.
     */
    SAME_LOWER_LVL,
    /**
     * Opat na rovnakej urovni, plus na vyssej, tzn. na rodicoch v stromovej
     * hierarchii modelu zdrojovych kodov (polozky na ceste od korena ku kotve).
     */
    SAME_HIGHER_LVL,
    /**
     * Iba na vyssej urovni v hierarchii zdrojovych suborov.
     */
    HIGHER_LVL,
    /**
     * Uvedena konfiguracna informacia je urcena anotaciou, ktora je na nizsej
     * urovni ako anotacia (resp. jej vlastnost), na ktorej zavisi.
     */
    LOWER_LVL,
    /**
     * V pripade, ze anotacia nie je nejako zavisla na kotve, ze nas kotva
     * nezaujima, alebo ze polozka ma informacie potrebne na spravne namapovanie
     * urcene inak.
     */
    NONE
}