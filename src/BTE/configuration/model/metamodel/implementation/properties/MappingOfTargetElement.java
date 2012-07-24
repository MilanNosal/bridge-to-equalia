package BTE.configuration.model.metamodel.implementation.properties;

import BTE.configuration.model.metamodel.enums.QNameProcessing;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import java.lang.annotation.ElementType;

/**
 * Trieda popisuje, ako sa ma mapovat informacia o nazve cieloveho jazykoveho
 * elementu.
 * @author Milan
 */
public class MappingOfTargetElement {
    // **************  Spracovanie celeho nazvu target elementu **************
    // Typ mapovania nazvu jazykoveho elementu
    private QNameProcessing QNameOfTargetProcType;

    // Typ polozky v XML
    private XMLProcessing QNameOfTargetProcView;

    // Typ mapovanie na nazov polozky v XML
    private TargetNameType targetNameType;

    // Retazec pouzivatelskeho nazvu polozky v XML pre mapovanie
    private String targetElementName = null;
    
    // Zoznam typov cielovych jazykovych elementov
    private ElementType[] targetElements;
    // *******************************************************************

    /**
     * Konstruktor.
     * @param QNameOfTargetProcType Typ mapovania nazvu jazykoveho elementu
     * @param QNameOfTargetProcView Typ polozky v XML
     * @param targetElementName Typ mapovanie na nazov polozky v XML
     * @param targetNameType Retazec pouzivatelskeho nazvu polozky v XML pre mapovanie
     * @param targetElements Zoznam typov cielovych jazykovych elementov
     */
    public MappingOfTargetElement(QNameProcessing QNameOfTargetProcType, XMLProcessing QNameOfTargetProcView, String targetElementName, TargetNameType targetNameType, ElementType[] targetElements) {
        this.QNameOfTargetProcType = QNameOfTargetProcType;
        this.QNameOfTargetProcView = QNameOfTargetProcView;
        this.targetElementName = targetElementName;
        this.targetNameType = targetNameType;
        this.targetElements = targetElements;
    }

    /**
     * Zjednoduseny konstruktor, ak nechceme mapovat cielovy jazykovy element.
     */
    public MappingOfTargetElement() {
        this(QNameProcessing.CONTEXT_PRINT, XMLProcessing.SKIP_PROCESS, null, TargetNameType.GENERIC, null );
    }

    /**
     * Typ urcujuci ako ma byt spracovane cele meno elementu kodu,
     * na ktory sa konf. informacia vztahuje. Vid popis
     * typu QNameProcessing. Presne identifikuje ci sa ma tlacit cela cesta,
     * len jednoduchy nazov, alebo ci sa tlacit nema nic.
     * @return
     */
    public QNameProcessing getQNameOfTargetProcType() {
        return QNameOfTargetProcType;
    }

    /**
     * Nastavenie typu mapovania nazvu.
     * @param QNameOfTargetProcType
     */
    public void setQNameOfTargetProcType(QNameProcessing QNameOfTargetProcType) {
        this.QNameOfTargetProcType = QNameOfTargetProcType;
    }

    /**
     * Vlastnost definujuca mapovanie na element, atribut alebo potlacajuca
     * mapovanie cieloveho jazykoveho elementu na XML.
     * @return
     */
    public XMLProcessing getQNameOfTargetProcView() {
        return QNameOfTargetProcView;
    }

    /**
     * Nastavenie typu mapovania cieloveho jazykoveho elementu.
     * @param QNameOfTargetProcView
     */
    public void setQNameOfTargetProcView(XMLProcessing QNameOfTargetProcView) {
        this.QNameOfTargetProcView = QNameOfTargetProcView;
    }

    /**
     * Definuje politiku mapovania na nazov v XML, genericke predstavuje
     * mapovanie na nazov typu jazykoveho elementu (t.j. podla aktualneho typu
     * pri konkretnej informacii), napr. nazov element class.
     * @return
     */
    public TargetNameType getTargetNameType() {
        return targetNameType;
    }

    /**
     * Nastavenie mapovania nazvu polozky v XML.
     * @param targetNameType
     */
    public void setTargetNameType(TargetNameType targetNameType) {
        this.targetNameType = targetNameType;
    }

    /**
     * Pouzivatelsky nazov polozky v XML, ma vyznam iba ak je TargetNameType
     * USER_DEFINED.
     * @return
     */
    public String getTargetElementName() {
        return targetElementName;
    }

    /**
     * Nastavenie pouzivatelskeho mena mapovania cieloveho jazykoveho elemenntu.
     * @param TargetElementName
     */
    public void setTargetElementName(String TargetElementName) {
        this.targetElementName = TargetElementName;
    }

    /**
     * Zoznam typov jazykovych elementov, nad ktorymi moze byt informacia
     * pouzita, nesmie byt null, ak je TargetNameType GENERIC. Ide o doplnok
     * k TargetElementName.
     * @return
     */
    public ElementType[] getTargetElements() {
        return targetElements;
    }

    /**
     * Nastavenie zoznamu typov cielovych jazykovych elementov.
     * @param targetElements
     */
    public void setTargetElements(ElementType[] targetElements) {
        this.targetElements = targetElements;
    }
}
