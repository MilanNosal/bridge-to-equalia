package BTE.configuration.model.metamodel.implementation.properties;

import BTE.configuration.model.metamodel.enums.RelativePositionToAnchor;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.metamodel.interfaces.InformationExtractor;
import javax.lang.model.element.ElementKind;

/**
 * Trieda zaobaluje informacie o mapovani konfiguracneho typu na zdrojove
 * texty.
 * @author Milan
 */
public class MappingOfConfigurationToSources {

    // **************  Urcenie typu zdroja informacie **************
    // Trieda definujuca zdroj polozky
    private final Class confAnnotation;

    // Cely nazov zdroja polozky
    private final String qualifiedNameOfSource;

    // Typ zdrojovej informacie, rozlisuje medzi vlastnostou, an. typom, pouzivatelskym typom a bez mapovania
    private final SourceType sourceType;
    // *******************************************************************

    // ************** Informacie pre najdenie zdroja informacie **************
    // Urcuje politiku hladania zdrojov vzhladom na "kotvu"
    private RelativePositionToAnchor relPositionToAnchor;

    // Kotva, podla ktorej sa hlada instancia informacie pre danu polozku
    private ConfigurationType positionAnchor;

    // Referencia na objekt, ktory umoznuje definovat pouzivatelske mapovanie na zdroje
    private InformationExtractor informationExtractor;

    // Zoznam mapovanych typov cielovych jazykovych elementov
    private ElementKind[] supportedSources = null;

    // Zoznam podporovanych prefixov cielovych jazykovych elementov
    private String[] supportedPrefix = null;

    // Pole dvoch boolovskych hodnot, prva polozka je priznakom o invertovani podporovanych
    // zdrojov, druha o invertovani podporovanych prefixov cielovych elementov
    final private boolean[] invert = new boolean[]{false, false};

    // *******************************************************************

    /**
     * Konstruktor.
     * @param confAnnotation Trieda definujuca zdroj polozky, viac v komentari k getConfAnnotation()
     * @param qualifiedNameOfSource Cely nazov zdroja polozky
     * @param sourceType Typ zdrojovej informacie, rozlisuje medzi vlastnostou, an. typom, pouzivatelskym typom a bez mapovania
     * @param relPositionToAnchor Urcuje politiku hladania zdrojov vzhladom na "kotvu"
     * @param positionAnchor Kotva, podla ktorej sa hlada instancia informacie pre danu polozku
     */
    public MappingOfConfigurationToSources(Class confAnnotation, String qualifiedNameOfSource, SourceType sourceType, RelativePositionToAnchor relPositionToAnchor, ConfigurationType positionAnchor) {
        this.sourceType = sourceType;
        this.confAnnotation = confAnnotation;
        this.qualifiedNameOfSource = qualifiedNameOfSource;
        this.relPositionToAnchor = relPositionToAnchor;
        this.positionAnchor = positionAnchor;
    }

    /**
     * Zjednoduseny konstruktor, ak nas nezaujima kotva.
     * @param confAnnotation
     * @param qualifiedNameOfSource
     * @param sourceType
     */
    public MappingOfConfigurationToSources(Class confAnnotation, String qualifiedNameOfSource, SourceType sourceType) {
        this(confAnnotation, qualifiedNameOfSource, sourceType, RelativePositionToAnchor.NONE, null);
    }

    /**
     * Objekt triedy, ktora predstavuje anotacny typ, na ktory sa konfiguracny
     * typ mapuje v zdrojovych textoch (pri sourceType==ANNOTATION).
     * Ak by sourceType bol DECLARED_PROPERTY, islo by o triedu (resp. wrapper)
     * ktora definuje tuto vlastnost. T.j. ak by bol anotacny typ Ann1 s
     * vlastnost prop1 typu Ann2 (iny anotacny typ), ktory by mal vlastnost
     * prop2, tak polozka pre prop2 by mala v confAnnotation triedu Ann2
     * (pretoze ta ju definuje), polozka pre prop1 by mala v confAnnotation
     * triedy Ann1 a rovnako aj polozka pre Ann1 by mala v confAnnotation
     * triedu Ann1.
     * @return
     */
    public Class getConfAnnotation() {
        return confAnnotation;
    }

    /**
     * String reprezentujuci nazov elementu kodu, ktory sluzi na urcenie
     * instancie. V podstate nazov confAnnotation. Rozdiel v pripade, ze ide
     * o deklarovanu vlastnost, vtedy sa uvadza meno vlastnosti, nie
     * referencie v confAnnotation.
     * Napr.:
     * balik.Trieda.metoda(java.lang.String) - ziskana pomocou Utilities.getMethodsCanonicalName
     *      ide o nazov bez modifikatorov a throws klauzuly
     * Pri vlastnosti je to dolezity prvok, pomocou neho ziskam definiciu
     * metody, z ktorej taham informacie.
     * Zaroven sluzi na identifikaciu ConfigurationType, aj v pripade, ze zdrojom nie
     * je anotacny typ, ale pouzivatelom definovany zdroj, je potrebne pouzit
     * nejaku nie null hodnotu, podla moznosti unikatnu.
     * @return
     */
    public String getQualifiedNameOfSource() {
        return qualifiedNameOfSource;
    }

    /**
     * Typ mapovaneho zdroja, ci ide o anotacny typ, deklarovanu vlastnost
     * nejakeho typu, pouzivatelom definovany zdroj alebo ze sa polozka
     * na zdroj nemapuje.
     * @return
     */
    public SourceType getSourceType() {
        return sourceType;
    }

    /**
     * Znacka, ktora hovori, kde hladat informaciu vhladom ku
     * kotve (positionAnchor). Dolezite aj pre urcenie typu hladaneho elementu.
     * Pouzivat v suhre s qualifiedNameOfSource a sourceType.
     * Totiz pri DECL_PROP sourceType nema zmysel nastavovat inu poziciu
     * ako NONE, pretoze kotva musi byt anotacia, ktora nesie vlastnost.
     * Vyuzitie najde pri anotacnych typoch a azda aj pri pouzivatelskych typoch.
     * Ak je napr. relPositionToAnchor LOWER_LVL, tak to znamena, ze informacie
     * tohto typu sa nachadzaju na nizsej(konkretnejsej) urovni, nez na akej
     * sa nachadza kotva. T.j. ak je kotva na balik.Trieda, tak tieto informacie
     * mame hladat na clenskych premennych a metodach(konstruktoroch), popr.
     * ich parametroch, tejto triedy.
     * @return
     */
    public RelativePositionToAnchor getRelPositionToAnchor() {
        return relPositionToAnchor;
    }

    /**
     * Nastavenie pozicie vhladom na kotvu.
     * @param relPositionToAnchor
     */
    public void setRelPositionToAnchor(RelativePositionToAnchor relPositionToAnchor) {
        this.relPositionToAnchor = relPositionToAnchor;
    }

    /**
     * Odkaz na objekt ConfigurationType, ktory je mozne pouzit pre najdenie
     * instancii tejto konf. informacie. Parser sa odraza pri
     * vytvarani instancii od tejto kotvy a vyuziva relativnu poziciu vzhladom
     * na rodica (relPositionToAnchor).
     * Ak ide o dekl. vlastnost, nesie odkaz na jej definujuci anotacny
     * typ.
     * @return
     */
    public ConfigurationType getPositionAnchor() {
        return positionAnchor;
    }

    /**
     * Nastavenie pozicnej kotvy.
     * @param positionAnchor
     */
    public void setPositionAnchor(ConfigurationType positionAnchor) {
        this.positionAnchor = positionAnchor;
    }

    /**
     * Umoznuje definovat pouzivatelsky typ mapovania na zdroje, relevantne
     * pri sourceType == USER_DEFINED. Vid informacie o rozhrani
     * InformationExtractor.
     * @return
     */
    public InformationExtractor getInformationExtractor() {
        return informationExtractor;
    }

    /**
     * Nastavenie referencie na implementaciu InformationExtractor.
     * @param informationExtractor
     */
    public void setInformationExtractor(InformationExtractor informationExtractor) {
        this.informationExtractor = informationExtractor;
    }

    /**
     * Zoznam podporovanych zdrojov. Ak ma napr. hodnotu {ElementKind.CLASS}, tak bude pre
     * tento konfiguracny typ nastroj mapovat iba anotacie (obmedzuje IBA v pripade anotacii,
     * vlastnosti ich vyskyt totiz priamo zavisia od anotacii),
     * ktore su ziskane z vyskytov nad uvedenym typom, t.j. nad triedou.
     * null hodnota znaci, ze nas obmedzenie zdrojov nezaujima.
     * @return
     */
    public ElementKind[] getSupportedSources() {
        return supportedSources;
    }

    /**
     * Nastavenie podporovanych zdrojovych typov.
     * @param supportedSources
     */
    public void setSupportedSources(ElementKind[] supportedSources) {
        this.supportedSources = supportedSources;
    }

    /**
     * Prefix zdroja (tj napr. prefix baliku, baliku pre triedu, balik a trieda pre metodu, a obd.),
     * pre ktory sa ma generovat informacia. Ak najdena anotacia je na cielovom
     * jazykovom elemente s prefixom, ktory je uvedeny v zozname, spracuje sa,
     * inak sa vynecha. Opat sa jedna iba o spracovanie anotacii a nie vlastnosti
     * (pri pouzivatelskych to zavisi od InformationExtractor). null hodnota
     * znaci, ze nas obmedzenie prefixov nezaujima.
     * @return
     */
    public String[] getSupportedPrefix() {
        return supportedPrefix;
    }

    /**
     * Nastavenie podporovanych prefixov.
     * @param supportedPrefix
     */
    public void setSupportedPrefix(String[] supportedPrefix) {
        this.supportedPrefix = supportedPrefix;
    }

    /**
     * Priznak, ci nema byt zoznam podporovanych zdrojov chapany invertovane,
     * t.j. ako zoznam NEpodporovanych zdrojov. Ak je true, potom sa spracuju
     * vsetky vyskyty nad zdrojmi, ktore nepatria do zoznamu podporovanych.
     * @return
     */
    public boolean getInvertSupportedSources(){
        return this.invert[0];
    }

    /**
     * Nastavenie priznaku o invertovani podporovanych zdrojov.
     * @param invert
     */
    public void setInvertSupportedSources(boolean invert){
        this.invert[0]=invert;
    }

    /**
     * Priznak, ci sa ma byt chapany zoznam podporovanych prefixov invertovane,
     * t.j. ako zoznam NEpodporovanych prefixov. Pri true hodnote by sa spracovali
     * vyskyty anotacii s cielovymi jazykovymi elementami, ktore by nemali prefix
     * v zozname.
     * @return
     */
    public boolean getInvertSupportedPrefix(){
        return this.invert[1];
    }

    /**
     * Nastavenie priznaku invertovania podporovanych prefixov.
     * @param invert
     */
    public void setInvertSupportedPrefix(boolean invert){
        this.invert[1]=invert;
    }
}
