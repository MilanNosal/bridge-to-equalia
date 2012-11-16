package BTE.configuration;

import BTE.configuration.communication.interfaces.AnnotationScanner;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.communication.scannotationscanner.ScannotationScanner;
import BTE.configuration.exceptions.MetaConfigurationException;
import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.metaconfiguration.implementation.BTEConfigurationLoader;
import BTE.configuration.metaconfiguration.implementation.MetaConfigurationGatherer;
import BTE.configuration.metaconfiguration.processors.ProcessorImpl;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.model.utilities.Utilities;
import BTE.configuration.parsing.metamodel.MetaModelParser;
import BTE.configuration.parsing.model.annotations.ModelParser;
import BTE.configuration.parsing.model.combining.MultiModelCombiner;
import BTE.configuration.parsing.model.xml.MultiModelParser;
import BTE.configuration.translation.metamodel.XSDTranslator;
import BTE.configuration.translation.model.XMLTranslator;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.w3c.dom.Document;

/**
 * Rozhranie pre nacitanie konfiguracie.
 * @author Milan
 */
public class ConfigurationGatherer {
    
    // <editor-fold defaultstate="collapsed" desc="Fields">
    // Metakonfiguracia
    MetaConfigurationLoader metaConfigurationLoader;
    // Anotacny skener
    AnnotationScanner annotationScanner;
    // Prekladac schemy
    XSDTranslator schema;
    // Prekladac vysledneho dokumentu
    XMLTranslator documentConfiguration;
    // Prekladac dokumentu iba pre konfiguraciu z anotacii
    XMLTranslator annotationsConfiguration;
    // Konfiguracia v JAXB objektoch
    JAXBElement jaxbConfiguration;
    // Volitelny procesor pre upravy mimo zakladnej konfiguracie
    Processor customProcessor = null;
    // </editor-fold>

    /**
     * Konstruktor. Parameter key predstavuje hodnotu key uvedenu v metakonfiguracnom
     * XML dokumente pri prislusnom elemente BTEConfiguration, resp. balik s
     * anotaciou BTEConfiguration.
     * @param key
     */
    public ConfigurationGatherer(String key) {
        MetaConfigurationGatherer mcg = new MetaConfigurationGatherer();
        this.metaConfigurationLoader = new BTEConfigurationLoader(key, mcg.getMetaconfiguration());
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(metaConfigurationLoader.getConfigurationAnnotationsNames());
        this.annotationScanner = new ScannotationScanner(annotations, metaConfigurationLoader.getWarningPrinter());
        initialize();
    }
    
    /**
     * Konstruktor. Parameter key predstavuje hodnotu key uvedenu v metakonfiguracnom
     * XML dokumente pri prislusnom elemente BTEConfiguration, resp. balik s
     * anotaciou BTEConfiguration. Parameter anotacneho skenera sluzi na prekrytie
     * predvoleneho skenera.
     * @param key
     */
    public ConfigurationGatherer(String key, AnnotationScanner as) {
        MetaConfigurationGatherer mcg = new MetaConfigurationGatherer();
        this.metaConfigurationLoader = new BTEConfigurationLoader(key, mcg.getMetaconfiguration());
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(metaConfigurationLoader.getConfigurationAnnotationsNames());
        this.annotationScanner = as;
        initialize();
    }

    /**
     * Konstruktor. Parameter key predstavuje hodnotu key uvedenu v metakonfiguracnom
     * XML dokumente pri prislusnom elemente BTEConfiguration, resp. balik s
     * anotaciou BTEConfiguration. Umoznuje pouzit pouzivatelom definovany
     * procesor metamodelu.
     * @param key
     * @param custom
     */
    public ConfigurationGatherer(String key, Processor custom) {
        this.customProcessor = custom;
        MetaConfigurationGatherer mcg = new MetaConfigurationGatherer();
        this.metaConfigurationLoader = new BTEConfigurationLoader(key, mcg.getMetaconfiguration());
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(metaConfigurationLoader.getConfigurationAnnotationsNames());
        this.annotationScanner = new ScannotationScanner(annotations, metaConfigurationLoader.getWarningPrinter());
        initialize();
    }

    /**
     * Konstruktor v pripade pouzitia nastroja napr. vo webovom prostredi, inak ziskava
     * URL na skenovanie. Vo webovom prostredi je pohodlne ziskat prislusne url
     * protrednictvom volania org.scannotation.WarUrlFinder.findWebInfClassesPath(servletContext).
     * Parameter key predstavuje hodnotu key uvedenu v metakonfiguracnom
     * XML dokumente pri prislusnom elemente BTEConfiguration, resp. balik s
     * anotaciou BTEConfiguration.
     * @param key
     * @param urlsToScan
     */
    public ConfigurationGatherer(String key, URL[] urlsToScan) {
        MetaConfigurationGatherer mcg = new MetaConfigurationGatherer(urlsToScan);
        this.metaConfigurationLoader = new BTEConfigurationLoader(key, mcg.getMetaconfiguration());
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(metaConfigurationLoader.getConfigurationAnnotationsNames());
        this.annotationScanner = new ScannotationScanner(urlsToScan, annotations, metaConfigurationLoader.getWarningPrinter());
        initialize();
    }

    /**
     * Konstruktor v pripade pouzitia nastroja napr. vo webovom prostredi, inak ziskava
     * URL na skenovanie. Vo webovom prostredi je pohodlne ziskat prislusne url
     * protrednictvom volania org.scannotation.WarUrlFinder.findWebInfClassesPath(servletContext).
     * Parameter key predstavuje hodnotu key uvedenu v metakonfiguracnom
     * XML dokumente pri prislusnom elemente BTEConfiguration, resp. balik s
     * anotaciou BTEConfiguration. Umoznuje pouzit pouzivatelom definovany procesor metamodelu.
     * @param key
     * @param servletContext
     * @param custom
     */
    public ConfigurationGatherer(String key, URL[] urlsToScan, Processor custom) {
        this.customProcessor = custom;
        MetaConfigurationGatherer mcg = new MetaConfigurationGatherer(urlsToScan);
        this.metaConfigurationLoader = new BTEConfigurationLoader(key, mcg.getMetaconfiguration());
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(metaConfigurationLoader.getConfigurationAnnotationsNames());
        this.annotationScanner = new ScannotationScanner(urlsToScan, annotations, metaConfigurationLoader.getWarningPrinter());
        initialize();
    }

    /**
     * Metoda ma znovu nacitat konfiguraciu (obnovit hodnotu, napr. po zmene
     * XML dokumentu).
     */
    public void refresh(){
        initialize();
    }

    /**
     * Vracia DOM model vyslednej konfiguracie.
     * @return
     */
    public Document getDOMConfiguration(){
        return documentConfiguration.getDocument();
    }

    /**
     * Vracia DOM model konfiguracie v anotaciach.
     * @return
     */
    public Document getDOMAnnotations(){
        return annotationsConfiguration.getDocument();
    }

    /**
     * Vracia DOM model prislusnej schemy.
     * @return
     */
    public Document getSchema(){
        return schema.getDocument();
    }

    /**
     * Vracia JAXB model konfiguracie.
     * @return
     */
    public Object getJAXBConfiguration(){
        return jaxbConfiguration.getValue();
    }

    /**
     * Vypise schemu do suboru podla metakonfiguracie.
     */
    public void outputSchema(){
        schema.outputDocument();
    }
    
    /**
     * Vypise schemu do zadaneho datoveho prudu podla metakonfiguracie.
     */
    public void outputSchema(PrintStream ps){
        schema.outputDocument(ps);
    }

    /**
     * Vypise dokument do suboru podla metakonfiguracie.
     */
    public void outputDOMConfiguration(){
        documentConfiguration.outputDocument();
    }
    
    /**
     * Vypise dokument do zadaneho datoveho prudu podla metakonfiguracie.
     */
    public void outputDOMConfiguration(PrintStream ps){
        documentConfiguration.outputDocument(ps);
    }

    /**
     * Vypise konfiguraciu z anotacii do vystupneho prudu.
     * @param outputStream
     */
    public void outputAnnotationsConfiguration(OutputStream outputStream){
        annotationsConfiguration.outputDocument(outputStream);
    }

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    /**
     * Metoda nacitava a spracuva konfiguraciu.
     */
    private void initialize(){
        // Vytvorim metamodel
        MetaModelParser parser = new MetaModelParser(metaConfigurationLoader);
        ConfigurationType configuration = parser.generateMetamodel();
        
        // Prevediem spracovanie metakonfiguracie ovplyvnujucej vzhlad metamodelu
        configuration = (new ProcessorImpl()).process(configuration);

        if(customProcessor!=null)
            configuration = customProcessor.process(configuration);
        // Finalne upravy metamodelu
        configuration = Utilities.replaceCodes(configuration);
        Utilities.orderModelByOrderPriority(configuration);
        // Priprava prekladaca metamodelu pre pripadny export schemy
        schema = new XSDTranslator(configuration, metaConfigurationLoader);

        // Generovanie modelu z anotacii
        ModelParser mp = new ModelParser
               (metaConfigurationLoader,
               annotationScanner,
               configuration);
        Information annotations = mp.parseModel();

        // Uz teraz prelozim ziskany model, pretoze pri spajani sa model znehodnoti
        // a uz to nebude mozne vykonat
        annotationsConfiguration = new XMLTranslator(annotations, metaConfigurationLoader);

        // Spracujem dokumenty z metakonfiguracie
        MultiModelParser mmp = new MultiModelParser();
        Information[] xmls = mmp.parseModels(configuration, metaConfigurationLoader);

        // A spojim ich aj s modelom anotacii
        Information merged = MultiModelCombiner.combineModels(metaConfigurationLoader, annotations, xmls);

        // Od teraz su annotations a kazdy z xmls znehodnotene, radsej priradim null
        annotations = null; xmls = null;

        // Prelozim vysledny model
        documentConfiguration = new XMLTranslator(merged, metaConfigurationLoader);

        // A nacitat do jaxb objekty, ale iba ak pouzivatel uviedol jaxbPackage, tzn. ze chce pracovat aj s jaxb
        if(metaConfigurationLoader.getJaxbPackage()==null || metaConfigurationLoader.getJaxbPackage().equals("")){
            return;
        }
        
        // Citanie do jaxb
        try {
            // Pri citani z anotacneho procesoru robilo somariny tak to skusam s classloadermi
            JAXBContext jc;
            try {
                jc = JAXBContext.newInstance(metaConfigurationLoader.getJaxbPackage(), this.getClass().getClassLoader());                
            } catch (Exception ex) {
                try {
                    jc = JAXBContext.newInstance(metaConfigurationLoader.getJaxbPackage(), Thread.currentThread().getContextClassLoader());
                } catch (Exception ex2) {
                    jc = JAXBContext.newInstance(metaConfigurationLoader.getJaxbPackage());                    
                }
            }
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            jaxbConfiguration = (JAXBElement) unmarshaller.unmarshal(schema.getDocument());
        } catch (JAXBException ex) {
            throw new MetaConfigurationException("ConfigurationGatherer:: ERROR:\n\t"
                        + "Some error with JAXB." ,ex);
        }
    }
    // </editor-fold>
}
