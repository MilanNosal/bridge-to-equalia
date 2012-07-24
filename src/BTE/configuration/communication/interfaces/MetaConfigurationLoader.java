package BTE.configuration.communication.interfaces;

import BTE.configuration.communication.Priority;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.List;
import javax.lang.model.element.ElementKind;

/**
 * Trieda na ziskanie metakofiguracie.
 * @author Milan
 */
public interface MetaConfigurationLoader {
    /**
     * Prvou metodou je metoda, ktora vrati triedy anotacnych typov, ktore
     * maju byt spracovane ako konfiguracne. Potrebujem zabezpecit stale poradie,
     * preto List, tym ostava poradie urcene metakonfiguraciou.
     * @return
     */
    public List<Class> getConfigurationAnnotations();

    /**
     * Prvou metodou je metoda, ktora vrati nazvy tried anotacnych typov, ktore
     * maju byt spracovane ako konfiguracne.
     * @return
     */
    public List<String> getConfigurationAnnotationsNames();

    /**
     * Vrati nazov korenoveho elementu pre generovany XML dokument (teda pre
     * vystupny DOM model).
     * @return
     */
    public String getRootName();

    /**
     * Vrati menny priestor generovaného XML dokumentu.
     * @return
     */
    public String getXMLNamespace();

    /**
     * Vrati zoznam vstupnych prudov, z ktorych sa zikaju vstupne XML dokumenty.
     * Predvolene zavisi na poradi, podla toho je urcovana priorita, ak nie
     * je explicitne povedane inak, prvy dokument v poradi ma najvyssiu
     * prioritu, ta dalej klesa.
     * @return
     */
    public List<InputStream> getDocuments();

    /**
     * Nazov (cesta) priecinku pre vystup.
     * @return
     */
    public String outputDirectory();

    /**
     * Vrati nazov suboru schemy aj s cestou relativnou k dokumentu. Potrebne
     * pre vlozenie referencie na schemu do dokumentu.
     * @return
     */
    public String getSchemaLocationToDocument();

    /**
     * Metoda vrati zoznam textovych reprezentacii typov elementov zdrojoveho
     * kodu uvedenych ako argumenty. Predvolene ide o anglicke nazvy zacinajuce
     * malymi pismenami. Mozne zmenit v konfiguracii.
     * Default:
     * if(elementType.equals(ElementType.ANNOTATION_TYPE))
            return new String[]{"annotation"};
        if(elementType.equals(ElementType.PACKAGE))
            return new String[]{"package"};
        if(elementType.equals(ElementType.TYPE))
            return new String[]{"class","enum","interface","annotation"};
        if(elementType.equals(ElementType.FIELD))
            return new String[]{"field"};
        if(elementType.equals(ElementType.METHOD))
            return new String[]{"method"};
        if(elementType.equals(ElementType.PARAMETER))
            return new String[]{"parameter"};
        if(elementType.equals(ElementType.CONSTRUCTOR))
            return new String[]{"constructor"};
        return new String[]{""};
     * @return
     */
    public String[] getSourceElementTypesAsStrings(ElementType[] elementTypes);

    /**
     * Metoda vracia zoznam vsetkych metakonfiguracnych informacii nad
     * vlastnostou nejakeho anotacneho typu.
     * @param property
     * @return
     */
    public List<Object> getMetaConfigurationsFor(Method property);

    /**
     * Metoda vracia zoznam vsetkych metakonfiguracnych informacii nad
     * nejakym anotacnym typom.
     * @param annType
     * @return
     */
    public List<Object> getMetaConfigurationsFor(Class annType);

    /**
     * Vrati objekt rozhrania IPrintStream pre vypis varovani.
     * @return
     */
    public IPrintStream getWarningPrinter();

    /**
     * Vrati textovu reprezentaciu nazvu druhu Java elementu.
     * Ak je parameter null, vrati ""-prazdny retazec.
     *  >>> PACKAGE - package
     *  >>> ENUM - enum
     *  >>> CLASS - class
     *  >>> ANNOTATION_TYPE - annotation
     *  >>> INTERFACE - interface
     *  >>> ENUM_CONSTANT - Exception/""
     *  >>> FIELD - field
     *  >>> PARAMETER - parameter
     *  >>> LOCAL_VARIABLE - Exception/""
     *  >>> EXCEPTION_PARAMETER - Exception/""
     *  >>> METHOD - method
     *  >>> CONSTRUCTOR - constructor
     *  >>> STATIC_INIT - Exception/""
     *  >>> INSTANCE_INIT - Exception/""
     *  >>> TYPE_PARAMETER - Exception/""
     *  >>> OTHER - Exception/""
     *  >>> null - ""
     * @param elementKind
     * @return
     */
    public String getElementKind(ElementKind elementKind);

    /**
     * Vracia určenie priority pre XML alebo anotácie.
     * @return
     */
    public Priority getPriority();

    /**
     * Nazov vystupneho suboru, ktory bude pouzity pri generovani XML.
     * @return
     */
    public String getFilenameOfOutput();

    /**
     * Vrati nazov balika, ktory obsahuje triedy pre jaxb.
     * @return
     */
    public String getJaxbPackage();
}
