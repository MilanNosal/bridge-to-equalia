package BTE.configuration.translation.metamodel;

import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.translation.enums.TranslatedConfigurationType;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * Trieda zapuzdrujuca potrebne elementy, ktore definuju prekladany konfiguracny
 * typ.
 * @author Milan
 */
public class TranslatedConfiguration {
    // Enumeracny typ, ktory mi poradi ako s danym typom narabat
    private TranslatedConfigurationType type;

    // Element definujuci vyskyt polozky tohto typu
    private Element element;

    // Nazov typu, ktory bude element pouzivat
    private String elementTypeName;

    // Typ spracovania tejto konfiguracie
    private XMLProcessing xmlType;

    // Element predstavujuci typ
    private Element elementType = null;

    // Element pre pridavanie potomkov typu atribut
    private Element addAttributesElement = null;

    // Element pre pridavanie potomkov typu element
    private Element addElementsElement = null;

    // Pomocny element pri skladani typov
    private Element helpElementType = null;

    // Rodic prelozenej polozky
    private TranslatedConfiguration parent;

    // Potomkovia prelozenej polozky
    private List<TranslatedConfiguration> children = new ArrayList<TranslatedConfiguration>();

    /**
     * Prazdny konstruktor.
     */
    public TranslatedConfiguration() {
    }

    /**
     * Vracia element na pridavanie atributov. Pri {COMPLEX_SEQ,
     * COMPLEX_EMPTY, COMPLEX_MIXED} to vlastne bude samotny complexType,
     * teda ten isty element ktory je pod clenom elementType.
     * Pri SIMPLE a ATTRIBUTE_SIMPLE nebude mat vyznam, niet co pridavat (bude null).
     * Pre SIMPLE_CONTENT a SIMPLE_CONT_RES tu bude odkaz na extension, kam
     * budem moct pridavat dalsie atributy.
     * @return
     */
    public Element getAddAttributesElement() {
        return addAttributesElement;
    }

    /**
     * Nastavenie referencie na element, pod ktory sa maju ukladat vyskyty
     * potomkov typu atribut.
     * @param addAttributesElement
     */
    public void setAddAttributesElement(Element addAttributesElement) {
        this.addAttributesElement = addAttributesElement;
    }

    /**
     * Element pre pridavanie dalsich elementov. V principe pojde o element
     * sequence {COMPLEX_SEQ, COMPLEX_MIXED}.
     * Pri SIMPLE a ostatne nebude mat vyznam, niet co pridavat, teda bude mat
     * hodnotu null.
     * @return
     */
    public Element getAddElementsElement() {
        return addElementsElement;
    }

    /**
     * Nastavenie referencie na element, pod ktory sa maju ukladat vyskyty
     * potomkov typu element.
     * @param addElementsElement
     */
    public void setAddElementsElement(Element addElementsElement) {
        this.addElementsElement = addElementsElement;
    }

    /**
     * Vracia element, ktory predstavuje vyskyt elementu (atributu) tohto typu,
     * tento sa napr. prida do definicie typu, ktory je rodicom tohto typu.
     * Pomocou tohto elementu viem povedat, ze niekde sa vyskytne element/atribut
     * tohto typu (napr. v sequence elemente rodica).
     * @return
     */
    public Element getElement() {
        return element;
    }

    /**
     * Nastavenie elementu vyskytu.
     * @param element
     */
    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * Element predstavujuci samotny typ. Tento sa napoji
     * priamo na korenovy element schemy.
     * @return
     */
    public Element getElementType() {
        return elementType;
    }

    /**
     * Nastavenie elementu predstavujuceho typ.
     * @param elementType
     */
    public void setElementType(Element elementType) {
        this.elementType = elementType;
    }

    /**
     * Nazov typu (mal by byt konzistentny s elementType).
     * @return
     */
    public String getElementTypeName() {
        return elementTypeName;
    }

    /**
     * Nastavenie nazvu typu.
     * @param elementTypeName
     */
    public void setElementTypeName(String elementTypeName) {
        this.elementTypeName = elementTypeName;
    }

    /**
     * Momentalne ma napada jedina situacia, kedy budem tento typ potrebovat.
     * Pojde o extension nejakeho zakladneho typu, na ktory potom v hlavnom
     * type aplikujem restriction. Cielom tohto bude zabezpecit jednoduchy obsah s
     * restriciton. Tu teda bude druhy simple type typ, ktory bude obmezdeny s
     * restriction. Pouzitie elementu bude samozrejme s nazvom hlavneho typu,
     * tento je len pomocny. Pridavam na root schemy, ak nie je null.
     * @see TranslatedConfigurationType.SIMPLE_CONT_RES
     * @return
     */
    public Element getHelpElementType() {
        return helpElementType;
    }

    /**
     * Nastavenie pomocneho elementu pre typ.
     * @param helpElementType
     */
    public void setHelpElementType(Element helpElementType) {
        this.helpElementType = helpElementType;
    }

    /**
     * Typ typu schemy :).
     * @return
     */
    public TranslatedConfigurationType getType() {
        return type;
    }

    /**
     * Nastavenie typu.
     * @param type
     */
    public void setType(TranslatedConfigurationType type) {
        this.type = type;
    }

    /**
     * Potomkovia prelozenej polozky metamodelu.
     * @return
     */
    public List<TranslatedConfiguration> getChildren() {
        return children;
    }

    /**
     * Rodic v hierarchii prelozenych poloziek.
     * @return
     */
    public TranslatedConfiguration getParent() {
        return parent;
    }

    /**
     * Nastavenie rodica v hierarchii.
     * @param parent
     */
    public void setParent(TranslatedConfiguration parent) {
        this.parent = parent;
    }

    /**
     * Typ vystupu v XML, ci ide o ELEMENT alebo ATTRIBUTE.
     * @return
     */
    public XMLProcessing getXmlType() {
        return xmlType;
    }

    /**
     * Nastavenie typu.
     * @param xmlType
     */
    public void setXmlType(XMLProcessing xmlType) {
        this.xmlType = xmlType;
    }

    /**
     * Prekrytie equals.
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object){
        return (object instanceof TranslatedConfiguration && object!=null && (this.elementTypeName.equals(((TranslatedConfiguration)object).elementTypeName)) );
    }

    /**
     * Prekrytie hashCode.
     * @return
     */
    @Override
    public int hashCode() {
        return (this.elementTypeName != null ? this.elementTypeName.hashCode() : 0);
    }

    /**
     * ToString metoda pre testovacie ucely.
     * @return
     */
    @Override
    public String toString(){
        String ret = "\n"+this.elementTypeName;
        ret = ret+":"+this.xmlType+"{";
        for(TranslatedConfiguration trans : children){
            ret = ret + trans.toString();
        }
        ret = ret+"}";
        return ret;
    }
}
