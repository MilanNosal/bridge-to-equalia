package BTE.configuration.metaconfiguration.implementation;

import BTE.configuration.communication.interfaces.AnnotationScanner;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.communication.scannotationscanner.ScannotationScanner;
import BTE.configuration.exceptions.MetaConfigurationException;
import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.model.utilities.Utilities;
import BTE.configuration.parsing.metamodel.MetaModelParser;
import BTE.configuration.parsing.model.annotations.ModelParser;
import BTE.configuration.parsing.model.combining.ModelCombiner;
import BTE.configuration.parsing.model.combining.MultiModelCombiner;
import BTE.configuration.parsing.model.xml.MultiModelParser;
import BTE.configuration.translation.model.XMLTranslator;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.MetaconfigurationType;

/**
 * Trieda, ktora ma za ulohu vytvorit informacie k metakonfiguracii.
 * @author Milan
 */
public class MetaConfigurationGatherer {

    /**
     * Objekt predstavujuci model metakonfiguracie v projekte.
     */
    private MetaconfigurationType metaconfiguration = null;

    /**
     * Konstruktor sa postara o spojenie konfiguracii z xml a anotacii a pokusi
     * sa nacitat pomocou JAXB konfiguraciu.
     */
    public MetaConfigurationGatherer() {
        // Vytvorime objekt spristupnujuci metametakonfiguraciu
        MetaConfigurationLoader loader = new BTEMetaConfigurationLoader();
        // Vygenerujeme metamodel metakonfiguracie
        ConfigurationType metaModel = getMetaModel(loader);
        // Spojime modely ziskane z anotacii a z XML
        ModelCombiner modelMerger = new ModelCombiner(getAnnotations(metaModel, loader),
                getXML(metaModel, loader), loader.getPriority());
        Information model = modelMerger.getModel();

        XMLTranslator xmlTrans = new XMLTranslator(model, loader);
        // A nacitame cez jaxb objekty
        try {
            // Pri citani z anotacneho procesoru robilo somariny tak to skusam s classloadermi
            JAXBContext jc;
            try {
                jc = JAXBContext.newInstance(loader.getJaxbPackage(), this.getClass().getClassLoader());                
            } catch (Exception ex) {
                try {
                    jc = JAXBContext.newInstance(loader.getJaxbPackage(), Thread.currentThread().getContextClassLoader());
                } catch (Exception ex2) {
                    jc = JAXBContext.newInstance(loader.getJaxbPackage());                    
                }
            }
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            JAXBElement<MetaconfigurationType> object = (JAXBElement<MetaconfigurationType>) unmarshaller.unmarshal(xmlTrans.getDocument());
            this.metaconfiguration = object.getValue();
        } catch (Exception ex) {
            throw new MetaConfigurationException("MetaConfigurationGatherer:: ERROR:\n\t"
                    + "Some error with JAXB.", ex);
        }
    }

    /**
     * Konstruktor sa postara o spojenie konfiguracii z xml a anotacii a pokusi
     * sa nacitat pomocou JAXB konfiguraciu. Konstruktor pre pouzitie v
     * netradicnom prostredi, kedy je potrebne zadat url na prehladanie
     * explicitne - napr. web.
     *
     * @param urlsToScan
     */
    public MetaConfigurationGatherer(URL[] urlsToScan) {
        // Vytvorime objekt spristupnujuci metametakonfiguraciu
        MetaConfigurationLoader loader = new BTEMetaConfigurationLoader();
        // Vygenerujeme metamodel metakonfiguracie
        ConfigurationType metaModel = getMetaModel(loader);
        // Spojime modely ziskane z anotacii a z XML
        ModelCombiner modelMerger = new ModelCombiner(getAnnotations(metaModel, loader, urlsToScan),
                getXML(metaModel, loader), loader.getPriority());
        Information model = modelMerger.getModel();
        
        // K tomu potrebujeme DOM model
        XMLTranslator xmlTrans = new XMLTranslator(model, loader);
        
        // A nacitame cez jaxb objekty
        try {
            // Pri citani z anotacneho procesoru robilo somariny tak to skusam s classloadermi
            JAXBContext jc;
            try {
                jc = JAXBContext.newInstance(loader.getJaxbPackage(), this.getClass().getClassLoader());                
            } catch (Exception ex) {
                try {
                    jc = JAXBContext.newInstance(loader.getJaxbPackage(), Thread.currentThread().getContextClassLoader());
                } catch (Exception ex2) {
                    jc = JAXBContext.newInstance(loader.getJaxbPackage());                    
                }
            }
            
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            JAXBElement<MetaconfigurationType> object = (JAXBElement<MetaconfigurationType>) unmarshaller.unmarshal(xmlTrans.getDocument());
            this.metaconfiguration = object.getValue();
        } catch (Exception ex) {
            throw new MetaConfigurationException("MetaConfigurationGatherer:: ERROR:\n\t"
                    + "Some error with JAXB.", ex);
        }
    }

    /**
     * Vrati konfiguraciu v Java objektoch.
     *
     * @return
     */
    public MetaconfigurationType getMetaconfiguration() {
        return metaconfiguration;
    }

    /**
     * Vygeneruje model pre xml, ak je viac xml dokumentov, spoji ich s
     * prioritou klesajucov zlava do prava.
     *
     * @param metaModel
     * @param loader
     * @return
     */
    private Information getXML(ConfigurationType metaModel, MetaConfigurationLoader loader) {
        MultiModelParser mmp = new MultiModelParser();
        // Vygenerujem modely
        Information[] models = mmp.parseModels(metaModel, loader);
        // a spojim
        return MultiModelCombiner.combineModels(models);
    }

    /**
     * Metoda vygeneruje model pre anotacie.
     *
     * @param metaModel
     * @param loader
     * @return
     */
    private Information getAnnotations(ConfigurationType metaModel, MetaConfigurationLoader loader) {
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(loader.getConfigurationAnnotationsNames());
        AnnotationScanner as = new ScannotationScanner(annotations, loader.getWarningPrinter());
        ModelParser mp = new ModelParser(loader,
                as,
                metaModel);
        Information model = mp.parseModel();
        return model;
    }

    /**
     * Metoda vygeneruje model pre anotacie.
     *
     * @param metaModel
     * @param loader
     * @return
     */
    private Information getAnnotations(ConfigurationType metaModel, MetaConfigurationLoader loader, URL[] urlsToScan) {
        Set<String> annotations = new HashSet<String>();
        annotations.addAll(loader.getConfigurationAnnotationsNames());
        AnnotationScanner as = new ScannotationScanner(urlsToScan, annotations, loader.getWarningPrinter());
        ModelParser mp = new ModelParser(loader,
                as,
                metaModel);
        Information model = mp.parseModel();
        return model;
    }

    /**
     * Metoda vygeneruje metamodel metakonfiguracie.
     *
     * @return
     */
    private ConfigurationType getMetaModel(MetaConfigurationLoader loader) {
        // Najprv spracujeme anotacne typy na metamodel
        MetaModelParser parser = new MetaModelParser(loader);
        ConfigurationType configuration = parser.generateMetamodel();
        // Upravime metamodel aby zodpovedal navrhu
        Processor metaProcessor = new MetaProcessor(loader);
        configuration = metaProcessor.process(configuration);
        // Posledne upravy
        configuration = Utilities.replaceCodes(configuration);
        Utilities.orderModelByOrderPriority(configuration);
        // a koniec
        return configuration;
    }
}
