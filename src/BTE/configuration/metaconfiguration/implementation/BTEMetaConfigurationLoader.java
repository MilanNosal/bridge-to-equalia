package BTE.configuration.metaconfiguration.implementation;

import BTE.configuration.communication.Priority;
import BTE.configuration.communication.WarningPrinter;
import BTE.configuration.communication.interfaces.IPrintStream;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.MetaConfigurationException;
import BTE.configuration.metaconfiguration.annotations.MapsTo;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;

/**
 * Trieda definujuca metametakonfiguraciu (metakonfiguraciu nastroja), poskytuje
 * informacie potrebne pre spracovanie metakonfiguracie (konfiguracie nastroja).
 *
 * @author Milan
 */
public class BTEMetaConfigurationLoader implements MetaConfigurationLoader {

    /**
     * Oddelovac poloziek zoznamu v properties subore.
     */
    private String separator;
    /**
     * Mnozina konf. anotacnych typov.
     */
    private List<Class> confAnnotations = new ArrayList<Class>();
    /**
     * Vypis varovani je predvolene nastaveny, aby pouzivatela varoval pri
     * chybach konfigurovania nastroja.
     */
    private boolean printing = true;
    /**
     * Nazov suboru s properties.
     */
    private final String propertiesFile = "metametaconfiguration.properties";
    /**
     * Objekt properties s nastaveniami.
     */
    private final Properties properties = new Properties();
    /**
     * Tabulka typov jazykovych elementov s mapovanim.
     */
    @SuppressWarnings("MapReplaceableByEnumMap")
    Map<ElementType, String[]> elementTypes = new HashMap<ElementType, String[]>();
    /**
     * Tabulka druhov jazykovych elementov s mapovanim.
     */
    @SuppressWarnings("MapReplaceableByEnumMap")
    Map<ElementKind, String> elementKinds = new HashMap<ElementKind, String>();
    /**
     * Nazov korenoveho elementu.
     */
    private String rootname;
    /**
     * Menny priestor metakonfiguracneho dokumentu.
     */
    private String namespace;
    /**
     * Nazov suboru s metakonfiguraciou.
     */
    private String filename;
    /**
     * Prud na vypis varovani.
     */
    private IPrintStream warningPrinter;
    /**
     * Priorita pri metakonfiguracii.
     */
    private Priority priority;
    /**
     * Balik pre JAXB.
     */
    private String jaxb;
    /**
     * Cesta pre vystupny priecinok.
     */
    private String outputPath;
    private static String separator_key = "separator";
    private static String rootname_key = "root-name";
    private static String namespace_key = "xml-namespace";
    private static String filename_key = "document-filename";
    private static String conf_annotations_key = "configuration-annotations";
    private static String ann_element_key = "annotation-element";
    private static String pack_element_key = "package-element";
    private static String class_element_key = "class-element";
    private static String enum_element_key = "enum-element";
    private static String interface_element_key = "interface-element";
    private static String field_element_key = "field-element";
    private static String param_element_key = "parameter-element";
    private static String meth_element_key = "method-element";
    private static String const_element_key = "constructor-element";
    private static String print_warns_key = "print-warnings";
    private static String priority_key = "priority";
    private static String output_key = "output-path";
    private static String jaxb_key = "jaxb-pack";

    /**
     * Konstruktor spracuje properties subor.
     */
    public BTEMetaConfigurationLoader() {
        try {
            this.properties.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
        } catch (Exception ex) {
            throw new MetaConfigurationException("MMConfigurationLoader::\n ERROR: "
                    + "IO error while trying to open \"" + this.propertiesFile + "\".");
        }

        initializePrinting();
        warningPrinter = new WarningPrinter(printing);

        separator = properties.getProperty(separator_key);
        if (separator == null || separator.equals("")) {
            separator = ";";
            printWarning("Key \'" + separator_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used separator \"" + separator + "\".");
        }

        rootname = properties.getProperty(rootname_key);
        if (rootname == null || rootname.equals("")) {
            rootname = "metaconfiguration";
            printWarning("Key \'" + rootname_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used rootname \"" + rootname + "\".");
        }

        namespace = properties.getProperty(namespace_key);
        if (namespace == null || namespace.equals("")) {
            namespace = "http://kpi.fei.tuke.sk/Nosal/Milan/BTE/metaconfiguration";
            printWarning("Key \'" + namespace_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used namespace \"" + namespace + "\".");
        }

        filename = properties.getProperty(filename_key);
        if (filename == null || filename.equals("")) {
            filename = "metaconfiguration";
            printWarning("Key \'" + filename_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used filename \"" + filename + "\".");
        }

        String help = properties.getProperty(priority_key);
        if (help == null || help.equals("")) {
            priority = Priority.XML;
            printWarning("Key \'" + priority_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used priority \"" + priority + "\".");
        } else {
            if (help.equals("annotations")) {
                priority = Priority.ANNOTATIONS;
            } else {
                priority = Priority.XML;
            }
        }

        outputPath = properties.getProperty(output_key);
        if (outputPath == null || outputPath.equals("")) {
            outputPath = "";
            printWarning("Key \'" + output_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used filename \"" + outputPath + "\".");
        }

        jaxb = properties.getProperty(jaxb_key);
        if (jaxb == null || jaxb.equals("")) {
            jaxb = "sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration";
            printWarning("Key \'" + jaxb_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used filename \"" + jaxb + "\".");
        }

        initializeConfigurationAnnotations();
        initializeElementTypes();
    }

    /**
     * Metoda spracuje zoznam metakonfiguracnych anotacnych typov.
     */
    private void initializeConfigurationAnnotations() {
        String annTypes = properties.getProperty(conf_annotations_key);
        if (annTypes == null || annTypes.equals("")) {
            printWarning("You have not specified any configuration annotations "
                    + "in \"" + this.propertiesFile + "\" under key \'" + conf_annotations_key + "\'.");
            annTypes = "";
        }
        for (String name : tokenize(annTypes)) {
            try {
                confAnnotations.add(Class.forName(name.trim()));
            } catch (ClassNotFoundException ex) {
                try {
                    confAnnotations.add(Class.forName(name.trim(), false, Thread.currentThread().getContextClassLoader()));
                } catch (ClassNotFoundException ex1) {
                    try {
                        confAnnotations.add(Class.forName(name.trim(), false, this.getClass().getClassLoader()));
                    } catch (ClassNotFoundException ex2) {
                        printWarning("Class \"" + name + "\", you have determined "
                                + "as configuration annotation type cannot be loaded.");
                    }
                }
            }
        }
    }

    /**
     * Metoda na parsovanie a inicializovanie mapovania druhov a typov
     * jazykovych elementov.
     */
    private void initializeElementTypes() {
        String[] type = new String[4];
        String value = null;
        value = this.properties.getProperty(ann_element_key);
        if (value == null || value.equals("")) {
            value = "annotation";
            printWarning("Key \'" + ann_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.ANNOTATION_TYPE, value);
        elementTypes.put(ElementType.ANNOTATION_TYPE, new String[]{value});
        type[0] = value;

        value = this.properties.getProperty(pack_element_key);
        if (value == null || value.equals("")) {
            value = "package";
            printWarning("Key \'" + pack_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.PACKAGE, value);
        elementTypes.put(ElementType.PACKAGE, new String[]{value});

        value = this.properties.getProperty(class_element_key);
        if (value == null || value.equals("")) {
            value = "class";
            printWarning("Key \'" + class_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.CLASS, value);
        type[1] = value;

        value = this.properties.getProperty(enum_element_key);
        if (value == null || value.equals("")) {
            value = "enum";
            printWarning("Key \'" + enum_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.ENUM, value);
        type[2] = value;

        value = this.properties.getProperty(interface_element_key);
        if (value == null || value.equals("")) {
            value = "interface";
            printWarning("Key \'" + interface_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.INTERFACE, value);
        type[3] = value;
        elementTypes.put(ElementType.TYPE, type);

        value = this.properties.getProperty(field_element_key);
        if (value == null || value.equals("")) {
            value = "field";
            printWarning("Key \'" + field_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.FIELD, value);
        elementTypes.put(ElementType.FIELD, new String[]{value});

        value = this.properties.getProperty(meth_element_key);
        if (value == null || value.equals("")) {
            value = "method";
            printWarning("Key \'" + meth_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.METHOD, value);
        elementTypes.put(ElementType.METHOD, new String[]{value});

        value = this.properties.getProperty(param_element_key);
        if (value == null || value.equals("")) {
            value = "parameter";
            printWarning("Key \'" + param_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.PARAMETER, value);
        elementTypes.put(ElementType.PARAMETER, new String[]{value});

        value = this.properties.getProperty(const_element_key);
        if (value == null || value.equals("")) {
            value = "constructor";
            printWarning("Key \'" + const_element_key + "\' was not set, used default"
                    + " value \"" + value + "\"");
        }
        elementKinds.put(ElementKind.CONSTRUCTOR, value);
        elementTypes.put(ElementType.CONSTRUCTOR, new String[]{value});
    }

    /**
     * Metoda pre inicializaciu priznak na vypis varovani.
     */
    private void initializePrinting() {
        String print = properties.getProperty(print_warns_key);
        boolean printWarns = true;
        if (print == null || print.equals("")) {
            printWarning("Key \'" + print_warns_key + "\' was not founded in \"" + this.propertiesFile + "\", or"
                    + " was empty. Instead used value \"" + printWarns + "\".");
            print = "true";
        }
        printing = (print.equals("true")) ? true : false;
    }

    /**
     * Metoda na vytvorenie pola retazcov zo zoznamu.
     *
     * @param string
     * @return
     */
    private String[] tokenize(String string) {
        StringTokenizer tokenizer = new StringTokenizer(string, separator);
        String[] array = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            array[i++] = tokenizer.nextToken();
        }
        return array;
    }

    public List<Class> getConfigurationAnnotations() {
        return confAnnotations;
    }

    public String getRootName() {
        return rootname;
    }

    public String getXMLNamespace() {
        return namespace;
    }

    public String[] getSourceElementTypesAsStrings(ElementType[] types) {
        Set<String> ret = new HashSet<String>();
        for (ElementType type : types) {
            if (type == ElementType.LOCAL_VARIABLE) {
                continue;
            }
            ret.addAll(Arrays.asList(elementTypes.get(type)));
        }
        return ret.toArray(new String[]{});
    }

    public IPrintStream getWarningPrinter() {
        return warningPrinter;
    }

    /**
     * Metoda na vypis varovania.
     *
     * @param cause
     */
    private void printWarning(String cause) {
        warningPrinter.println("MMConfigurationLoader::\n WARNING: " + cause);
    }

    public List<Object> getMetaConfigurationsFor(Method declMethod) {
        MapsTo mapsTo = (MapsTo) declMethod.getAnnotation(MapsTo.class);
        List<Object> list = new ArrayList<Object>();
        if (mapsTo != null) {
            list.add(mapsTo);
        }
        return list;
    }

    public List<Object> getMetaConfigurationsFor(Class annType) {
        MapsTo mapsTo = (MapsTo) annType.getAnnotation(MapsTo.class);
        List<Object> list = new ArrayList<Object>();
        if (mapsTo != null) {
            list.add(mapsTo);
        }
        return list;
    }

    public String getElementKind(ElementKind elementKind) {
        if (elementKind == null) {
            return "";
        }
        if (!this.elementKinds.containsKey(elementKind)) {
            return "";
        }
        return this.elementKinds.get(elementKind);
    }

    public Priority getPriority() {
        return priority;
    }

    public List<String> getConfigurationAnnotationsNames() {
        List<String> anns = new ArrayList<String>();
        for (Class clazz : this.confAnnotations) {
            anns.add(clazz.getName());
        }
        return anns;
    }

    public List<InputStream> getDocuments() {
        List<InputStream> list = new ArrayList<InputStream>();
        list.add(getClass().getClassLoader().getResourceAsStream(filename + ".xml"));
        return list;
    }

    public String getSchemaLocationToDocument() {
        return filename + ".xsd";
    }

    public String getFilenameOfOutput() {
        return filename + ".xml";
    }

    public String outputDirectory() {
        return outputPath;
    }

    public String getJaxbPackage() {
        return jaxb;
    }
}
