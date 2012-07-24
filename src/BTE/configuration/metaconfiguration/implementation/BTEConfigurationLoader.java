package BTE.configuration.metaconfiguration.implementation;

import BTE.configuration.communication.Priority;
import BTE.configuration.communication.WarningPrinter;
import BTE.configuration.communication.interfaces.IPrintStream;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.MetaConfigurationException;
import BTE.configuration.model.utilities.Utilities;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.*;
import javax.lang.model.element.ElementKind;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.*;

/**
 * Trieda pre nacitanie konfiguracie nastroja (teda metakonfiguracie)
 * prostrednictvom abstrakcie konfiguracie XML a anotacii.
 * @author Milan
 */
public class BTEConfigurationLoader implements MetaConfigurationLoader {
    /**
     * Model metakonfiguracie v Java objektoch.
     */
    private BTEConfigurationType source;

    /**
     * Mnozina tried konfiguracnych anotacnych typov.
     */
    private List<Class> confAnnotations = new ArrayList<Class>();

    /**
     * Prud na vypis varovani.
     */
    private WarningPrinter warningPrinter;

    /**
     * Tabulka mapovania typov jazykovych elementov.
     */
    @SuppressWarnings("MapReplaceableByEnumMap")
    Map<ElementType, String[]> elementTypes = new HashMap<ElementType, String[]>();

    /**
     * Dlzka kluca, klucom je nazov balika, ktory je oznaceny metainformaciou
     * BTEConfiguration. Sluzi mi neskor na hladanie spravnej vlastnosti resp.
     * anotacneho typu.
     */
    private int keyLength;

    /**
     * Konstruktor, ktory pre dany kluc vyberie z mct objektu podstrom
     * reprezentujuci konfiguraciu s danym klucom a nainicializuje potrebne
     * premenne.
     * @param key
     * @param mct Tento parameter je vhodne ziskat pouzitim MetaConfigurationGatherer.
     */
    public BTEConfigurationLoader(String key, MetaconfigurationType mct){
        for(BTEConfigurationType bteConfiguration : mct.getBTEConfiguration()){
            String tempKey = bteConfiguration.getKey();
            if(tempKey == null && key == null){
                source = bteConfiguration;
                this.keyLength = 0;
                init();
                return;
            }
            if(tempKey!=null && tempKey.equals(key)){
                this.keyLength = key.length();
                source = bteConfiguration;
                init();
                return;
            }
        }
        throw new MetaConfigurationException("BTEMetaConfigurationLoader()::\n\tERROR: Cannot find "
                + "configuration with key \""+key+"\".");
    }

    /**
     * Metoda inicializujuca clenske premenne.
     */
    private void init(){
        // priprava vypisu chyb
        warningPrinter = new WarningPrinter(source.isWarningPrinting());
        // Nainicializovanie zoznamu tried
        for(String clazz : source.getConfigurationAnnotations()){
            try {
                confAnnotations.add(Class.forName(clazz.trim(), false, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException ex) {
                try {
                    confAnnotations.add(Class.forName(clazz.trim()));
                } catch (ClassNotFoundException ex1) {
                    warningPrinter.println("BTEMetaConfigurationLoader::\n\tWarning: Class \""+clazz+"\", you have determined " +
                        "as configuration annotation type cannot be loaded.");
                }
            }
        }
        // priprava elementTypes
        elementTypes.put(ElementType.ANNOTATION_TYPE, new String[] { source.getDocument().getElementKinds().getAnnotationType() });
        elementTypes.put(ElementType.PACKAGE, new String[] { source.getDocument().getElementKinds().getPackage() });
        elementTypes.put(ElementType.TYPE, new String[] {
            source.getDocument().getElementKinds().getAnnotationType(),
            source.getDocument().getElementKinds().getClazz(),
            source.getDocument().getElementKinds().getInterface(),
            source.getDocument().getElementKinds().getEnum()});
        elementTypes.put(ElementType.FIELD, new String[] { source.getDocument().getElementKinds().getField() });
        elementTypes.put(ElementType.METHOD, new String[] { source.getDocument().getElementKinds().getMethod() });
        elementTypes.put(ElementType.PARAMETER, new String[] { source.getDocument().getElementKinds().getParameter() });
        elementTypes.put(ElementType.CONSTRUCTOR, new String[] { source.getDocument().getElementKinds().getConstructor() });
    }

    @Override
    public List<Class> getConfigurationAnnotations() {
        return confAnnotations;
    }

    @Override
    public List<String> getConfigurationAnnotationsNames() {
        return new ArrayList<String>(source.getConfigurationAnnotations());
    }

    @Override
    public String getRootName() {
        return source.getDocument().getRootName();
    }

    @Override
    public String getXMLNamespace() {
        return source.getDocument().getXMLNameSpace();
    }

    @Override
    public List<InputStream> getDocuments() {
        // Vytvorime zoznam dokumentov
        List<InputStream> documents =  new ArrayList<InputStream>();
        for(InputDocumentsType idt : source.getInputDocuments()){
            if(idt.isResource()){
                // Ak ide o abstrakciu k zdrojom
                documents.add(Thread.currentThread().getContextClassLoader().getResourceAsStream(idt.getLocation()));
            } else {
                // inak skusame otvarat subor na citanie
                try {
                    documents.add(new FileInputStream(idt.getLocation()));
                } catch (FileNotFoundException ex){
                    warningPrinter.println("BTEMetaConfigurationLoader.getDocuments()::\n\tWarning: "
                            + "MetaconfigurationLoader cannot find source document \""+idt.getLocation()+"\".");
                }
            }
        }
        return documents;
    }

    @Override
    public String outputDirectory() {
        return source.getOutputDirectory();
    }

    @Override
    public String getSchemaLocationToDocument() {
        return source.getSchemaLocation();
    }

    @Override
    public String[] getSourceElementTypesAsStrings(ElementType[] types) {
        Set<String> ret = new HashSet<String>();
        for (ElementType type : types) {
            if(type==ElementType.LOCAL_VARIABLE)
                continue;
            ret.addAll(Arrays.asList(elementTypes.get(type)));
        }
        return ret.toArray(new String[]{});
    }

    @Override
    public List<Object> getMetaConfigurationsFor(Method property) {
        // Hladame, ci mame k dispozicii nejake metakonfiguraciu k vlastnosti
        String method = Utilities.getMethodsCanonicalName(property.toGenericString(), property.getName());
        // Z nazvu orezeme balik
        method = method.substring(keyLength+1);
        List<Object> list = new ArrayList<Object>();
        for(AnnotationTypeType att: source.getAnnotationType()){
            // Najprv najdeme jej anotacny typ
            if(method.startsWith(att.getAnnotation())){
                // A orezeme z nazvu metody aj ten
                method = method.substring(att.getAnnotation().length()+1);
                for(DeclMethodType dmt : att.getDeclMethod()){
                    // A mozeme porovnavat zvysok nazvu s hodnotou
                    if(method.equals(dmt.getMethod())){
                        list.addAll(dmt.getAttribute());
                        list.addAll(dmt.getInside());
                        list.addAll(dmt.getMapsTo());
                        list.addAll(dmt.getSkip());
                        list.addAll(dmt.getStaticModifier());
                        list.addAll(dmt.getTargetElement());
                        list.addAll(dmt.getWrapper());
                        list.addAll(dmt.getCopyBranch());
                        return list;
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<Object> getMetaConfigurationsFor(Class annType) {
        // Opat orezeme nazov balika, aby sa lahsie porovnavalo (ak kvoli
        // jednoznacnosti)
        String clazz = annType.getName();
        clazz = clazz.substring(keyLength+1);
        List<Object> list = new ArrayList<Object>();
        for(AnnotationTypeType att: source.getAnnotationType()){
            if(clazz.equals(att.getAnnotation())){
                list.addAll(att.getInside());
                list.addAll(att.getMapsTo());
                list.addAll(att.getSkip());
                list.addAll(att.getStaticModifier());
                list.addAll(att.getTargetElement());
                list.addAll(att.getWrapper());
                list.addAll(att.getCopyBranch());
                return list;
            }
        }
        return list;
    }



    @Override
    public IPrintStream getWarningPrinter() {
        return warningPrinter;
    }

    @Override
    public String getElementKind(ElementKind elementKind) {
        if(elementKind==null)
            return "";
        switch(elementKind){
            case ANNOTATION_TYPE:
                return source.getDocument().getElementKinds().getAnnotationType();
            case CLASS:
                return source.getDocument().getElementKinds().getClazz();
            case CONSTRUCTOR:
                return source.getDocument().getElementKinds().getConstructor();
            case ENUM:
                return source.getDocument().getElementKinds().getEnum();
            case FIELD:
                return source.getDocument().getElementKinds().getField();
            case INTERFACE:
                return source.getDocument().getElementKinds().getInterface();
            case METHOD:
                return source.getDocument().getElementKinds().getMethod();
            case PACKAGE:
                return source.getDocument().getElementKinds().getPackage();
            case PARAMETER:
                return source.getDocument().getElementKinds().getParameter();
            default:
                return "";
        }
    }

    @Override
    public Priority getPriority() {
        return (source.getPriority() == PriorityType.ANNOTATIONS)?Priority.ANNOTATIONS:Priority.XML;
    }

    @Override
    public String getFilenameOfOutput() {
        return source.getOutputDocument();
    }

    @Override
    public String getJaxbPackage() {
        return source.getJaxbPackage();
    }

}
