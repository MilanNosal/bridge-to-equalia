package BTE.configuration.model.metamodel.implementation.properties;

import BTE.configuration.model.metamodel.enums.TypeOfElement;

/**
 * Mapovanie konfiguracneho typu na XSD. Vyuzivam pri preklade metamodelu
 * do XML schemy. Zavisi od kontextu konfiguracneho typu, preto ak sa meni
 * kontext je potrebne pamatat na zmenu tejto polozky, inak nebude spravne
 * generovat XSD.
 * @author Milan
 */
public class MappingOfConfigurationToXSD {
    // ************** Typ **************
    // Obsah konfiguracneho typu v XSD
    private TypeOfElement typeOfElement;

    // Zakladny typ, resp. vymenovanie hodnot enumeracneho typu
    private String[] simpleTypeValue = new String[]{};
    // *******************************************************************

    /**
     * Konstruktor.
     * @param typeOfElement Typ konfiguracneho typu v XSD
     * @param simpleTypeValue Zakladny typ, resp. vymenovanie hodnot enumeracneho typu
     */
    public MappingOfConfigurationToXSD(TypeOfElement typeOfElement, String[] simpleTypeValue) {
        this.typeOfElement = typeOfElement;
        this.simpleTypeValue = simpleTypeValue;
    }

    /**
     * Sluzi pre urcenie typu obsahu polozky v XSD.
     * @return
     */
    public TypeOfElement getTypeOfElement() {
        return typeOfElement;
    }

    /**
     * Nastavenie typu polozky v XSD.
     * @param typeOfElement
     */
    public void setTypeOfElement(TypeOfElement typeOfElement) {
        this.typeOfElement = typeOfElement;
    }

    /**
     * V pripade, ze typeOfElement je BASIC_TYPE, touto clenskou premennou je
     * urceny konkretny zakladny typ (pole velkosti jeden). Ak typeOfElement je
     * ENUMERATION, uvadza vymenovanie povolenych hodnot. Pri primitivnych
     * typoch pouzijte boolean.class.getName(), podobne tu istu metodu pri
     * String.
     * @return
     */
    public String[] getSimpleTypeValue() {
        return simpleTypeValue;
    }

    /**
     * Nastavenie konkretneho zakladneho typu, alebo hodnot enumeracneho typu.
     * @param simpleTypeValue
     */
    public void setSimpleTypeValue(String[] simpleTypeValue) {
        this.simpleTypeValue = simpleTypeValue;
    }

}
