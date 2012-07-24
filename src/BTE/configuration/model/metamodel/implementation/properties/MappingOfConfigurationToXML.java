package BTE.configuration.model.metamodel.implementation.properties;

import BTE.configuration.model.metamodel.enums.GeneratingPolicy;
import BTE.configuration.model.metamodel.enums.XMLProcessing;

/**
 * Informacie o mapovani konfiguracneho typu na XML.
 * @author Milan
 */
public class MappingOfConfigurationToXML {
    // **************  Urcenie vzhladu reprezentacie informacie **************
    // Meno polozky v XML, na ktoru sa konf. typ mapuje
    private String name;

    // Predvolena hodnota
    private String defaultValue;

    // Mapovanie na element/atribut, potlacenie mapovania
    private XMLProcessing XMLOutputType;

    // Nazov typu v XSD, na ktory sa konf. typ mapuje
    private String typeName="${name}-type";

    // Hodnota priority v poradi
    private int orderPriority;

    // Minimalny pocet vyskytov poloziek
    private int minOccurs;

    // Maximalny pocet vyskytov poloziek
    private int maxOccurs;

    // Sposob generovania poloziek bez mapovania na zdroje
    private GeneratingPolicy generatingPolicy = GeneratingPolicy.PER_PARENT;

    // *******************************************************************

    /**
     * Konstruktor.
     * @param name Meno polozky v XML, na ktoru sa konf. typ mapuje
     * @param typeName Nazov typu v XSD, na ktory sa konf. typ mapuje
     * @param defaultValue Predvolena hodnota
     * @param XMLOutputType Mapovanie na element/atribut, potlacenie mapovania
     * @param orderPriority Priorita v poradi
     * @param minOccurs Minimalny pocet vyskytov poloziek
     * @param maxOccurs Maximalny pocet vyskytov poloziek
     */
    public MappingOfConfigurationToXML(String name, String typeName, String defaultValue, XMLProcessing XMLOutputType,  int orderPriority, int minOccurs, int maxOccurs) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.XMLOutputType = XMLOutputType;
        this.typeName = typeName;
        this.orderPriority = orderPriority;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    /**
     * Konstruktor s volitelnym pouzitim polozky a neobmedzenym poctom pouziti.
     * @param name
     * @param typeName
     * @param defaultValue
     * @param XMLOutputType
     * @param orderPriority
     */
    public MappingOfConfigurationToXML(String name, String typeName, String defaultValue, XMLProcessing XMLOutputType, int orderPriority) {
        this(name, typeName, defaultValue, XMLOutputType, orderPriority, 0, -1);
    }

    /**
     * Nazov XML elementu (atributu), na ktory ma byt konfiguracna informacia
     * mapovana. Predvolene je to jednoduchy nazov zdroja informacie.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Nastavenie nazvu mapovanej XML polozky.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Predvolena hodnota informacie, principialne ide o hodnotu atributu,
     * alebo textovy uzol XML. Podstatna je pri definovani predvolenej
     * hodnoty daneho elementu (attributu). Ak je null, informacia nema
     * prevolenu hodnotu.
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Nastavenie predvolenej hodnoty.
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Mapovanie daneho konfiguracneho typu. Rozhoduje, ci sa polozka mapuje do
     * XML na atribut alebo element, resp. ci sa vobec mapuje.
     * @return
     */
    public XMLProcessing getXMLOutputType() {
        return XMLOutputType;
    }

    /**
     * Nastavenie mapovania konfiguracneho typu.
     * @param XMLOutputType
     */
    public void setXMLOutputType(XMLProcessing XMLOutputType) {
        this.XMLOutputType = XMLOutputType;
    }

    /**
     * Nazov typu v scheme. Predvolene to bude nazov
     * konfiguracie zretazeny s "-type". Spojenie ${name} bude pri spracovani
     * nahradeny nazvom elementu (z ConfigurationView).
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Nastavenie nazvu typu v XSD.
     * @param typeName
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Priorita, podla ktorej budu usporiadane elementy v xsd aj v xml.
     * Moze byt pouzite ak chceme nastavit poradie elementov xml, tj ktory
     * element sa ma pridat skor a ktory neskor. Vyssia hodnota znamena
     * vyssiu prioritu, tzn. ze element s vyssou prioritou bude situovany pred
     * elementom s nizsou. Pre atributy nema vyznam.
     * @return
     */
    public int getOrderPriority() {
        return orderPriority;
    }

    /**
     * Nastavenie priority usporiadania.
     * @param orderPriority
     */
    public void setOrderPriority(int orderPriority) {
        this.orderPriority = orderPriority;
    }
    
    /**
     * Minimalny pocet vyskytov poloziek. Ak sa minOccurs uvedie ako 1+ pri
     * atribute, atribut bude oznaceny ako povinny.
     * Predvolene 0. Ak je hodnota zaporna, neuvedie sa (volitelnost).
     * @return
     */
    public int getMinOccurs() {
        return minOccurs;
    }

    /**
     * Nastavenie minimalnej hodnoty.
     * @param minOccurs
     */
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Maximalny pocet vyskytov poloziek. Pre atributy ma zmysel 0, ktora
     * nastavi use="prohibited".
     * Predvolene -1.
     * -1 == unbounded
     * m**Occurs < -1 == nechceme pridavat atributy pre minOccurs a maxOccurs
     * @return
     */
    public int getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Nastavenie maximalneho poctu vyskytov poloziek.
     * @param maxOccurs
     */
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * Politika, podla ktorej sa mapuju pouzivatelske typy elementov, tzv.
     * zaobalovacov, ktore nemaju mapovanie do zdrojovych textov. Urcuje, kolko
     * (a aj podla coho a akych) elementov ma byt nagenerovanych pri nacitavani
     * informacii zo zdrojovych textov. Nema zmysel pre atribut.
     * Predvolene 1 polozka pre 1 rodica. Pouzivat tak, aby sa nebilo s min
     * a maxOccurs (napr moze mat mnoho potomkov, a potom nemozem obmedzit
     * maxOccurs na nejaky konkretny pocet), a to ani u potomkov.
     * Odporucam v pripade perChild dat maxOccurs na -1, t.j. unbounded.
     * Budem brat v uvahu iba ak je SourceType.NONE v mapovani na zdroje,
     * tzn. ze iba vtedy, ak je to pridavany element, ktory sa neviaze priamo
     * na nejaku anotaciu.
     * Ak ma viac druhov potomkov (viac konfiguracnych typov), tak treba
     * brat ohlad aj na ich minOccurs, ak je perChild, tak v kazdom takomto
     * elemente sa vyskytne iba jedna informacia, a teda vsetci potomkovia musia
     * mat povolene byt optional.
     * @return
     */
    public GeneratingPolicy getGeneratingPolicy() {
        return generatingPolicy;
    }

    /**
     * Nastavenie politiky generovania poloziek bez mapovania na zdrojove
     * texty.
     * @param generatingPolicy
     */
    public void setGeneratingPolicy(GeneratingPolicy generatingPolicy) {
        this.generatingPolicy = generatingPolicy;
    }
}
