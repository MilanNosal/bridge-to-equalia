package BTE.configuration.model.model.interfaces;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Rozhranie pre konfiguracnu informaciu.
 * @author Milan
 */
public interface Information {
    /**
     * Vracia nazov danej informacie potrebny pre mapovanie do XML.
     * @return
     */
    String getName();

    /**
     * Retazec hodnoty danej informacie (ci uz elementu alebo atributu). Ak ide
     * o null, alebo "", potom sa hodnota nespracovava.
     * @return
     */
    String getValue();

    /**
     * Vracia typ mapovania do XML, bud na element alebo na atribut. Nemal by
     * vraciat typ SKIP_PROCESS, informacie typu, ktory sa nema spracovat nemaju
     * byt vobec generovane. Navratenie SKIP_PROCESS hodnoty moze vyvolat
     * vynimku.
     * @return
     */
    XMLProcessing getXMLProcessing();

    /**
     * Vracia odkaz na popis charakteru tejto informacie v metamodeli.
     * @return
     */
    ConfigurationType getMMConfiguration();

    /**
     * Vracia odkaz na instanciu anotacie, na ktoru sa informacia mapuje.
     * Moze obsahovat aj "dummy" instanciu, ktora v skutocnosti neobsahuje
     * anotaciu, iba informacie o elemente zdrojoveho kodu, na ktory sa
     * informacia mapuje (napr. pri wrapper-och).
     * @return
     */
    AnnotationTypeInstance getInformationSource();

    /**
     * Nastavenie instancie anotacie, podstatne pre nastavenie kontextu nielen
     * retazcovou reprezentaciou.
     * @param annotationInstance 
     */
    void setInformationSource(AnnotationTypeInstance annotationInstance);

    /**
     * Objekt, z ktoreho sa tahaju informacie, moze to by anotacia,
     * String, atd., proste vsetko co moze mat anotacia ako navratovu
     * hodnotu svojich deklarovanych vlastnosti. Sluzi najma pre reflexiu
     * pri citani deklarovanych vlastnosti anotacii. Napr. ak je nejaka informacia
     * mapovana na vlastnost, jej hodnota sa ziska z informacie pre kotvu. Kotva
     * vtedy musi mat v tejto vlastnosti ulozeny objekt anotacie, z ktoreho
     * sa potom prislusna vlastnost ziska pomocou reflexie (objekt Method vlastnosti
     * sa ziska z metamodelu, ale v sourceValue je skutocny objekt, na ktory
     * sa reflexiou bude dopytovat).
     * @return
     */
    Object getSourceValue();

    /**
     * Cele meno elementu kodu, ku ktoremu sa podla kontextu (resp. instancie
     * anotacie) vztahuje tato informacia.
     * @return
     */
    String getTargetQualifiedName();

    /**
     * Nastavenie celeho mena elementu zdrojoveho kodu, na ktory sa informacia
     * viaze.
     * @param targetQualifiedName
     */
    void setTargetQualifiedName(String targetQualifiedName);

    /**
     * Vyjadruje, aka hodnota nazvu cieloveho elementu sa byt pouzije v XML.
     * Ak sa nema pouzit, je vhodnejsie pouzit prazdny retazec ako null hodnotu.
     * @return
     */
    String getTargetElementValue();

    /**
     * Metoda na nastavenie hodnoty vyjadrenia cieloveho elementu.
     * @param targetElementValue
     */
    void setTargetElementValue(String targetElementValue);

    /**
     * Typ mapovania nazvu cieloveho elementu, t.j. ci sa mapuje na element,
     * atribut, alebo sa nespracovava.
     * @return
     */
    XMLProcessing getTargetElementProcessing();

    /**
     * Nazov atributu/elementu ktory urcuje cielovy element. Potrebne pre tzv.
     * GENERIC mapovanie, ked je mozne z konf. informacie ziskat viac roznych
     * mien. Vtedy je potrebne urcit jednoznacne jeden nazov.
     * Priklad: "interface"..
     * @return
     */
    String getTargetElementName();

    /**
     * Odkaz na rodica v modeli.
     * @return
     */
    Information getParent();

    /**
     * Nastavenie rodica v modeli.
     * @param parent
     */
    void setParent(Information parent);

    /**
     * Tabulka potomkov podla konfiguracneho typu, kvoli lahsiemu spracovavaniu
     * namiesto obycajneho zoznamu.
     * @return
     */
    Map<ConfigurationType, List<Information>> getChildren();

    /**
     * Nastavenie potomkov.
     * @param children
     */
    void setChildren(Map<ConfigurationType, List<Information>> children);
    
    /**
     * Metoda ma jednoduchu ulohu, vypisat rekurzivne model. Dolezite pre
     * pohodlny debugging.
     * @param ps Vystupny prud.
     * @param offset Pripadny offset vystupu, volitelny argument. POZOR!, moze byt null.
     */
    void print(PrintStream ps, String offset);
}
