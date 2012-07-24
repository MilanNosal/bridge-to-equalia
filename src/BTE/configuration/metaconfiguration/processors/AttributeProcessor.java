package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.GeneratingPolicy;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.AttributeType;

/**
 * Spracovanie metakonfiguracie pre mapovanie na atribut.
 * @author Milan
 */
public class AttributeProcessor implements Processor {
    public ConfigurationType process(ConfigurationType configuration){
        if(attributePresent(configuration)){
            configuration.getMappingOfConfigurationToXML().setXMLOutputType(XMLProcessing.ATTRIBUTE);
            // Atribut nema potomkov, takze vsetko pod nim preskocime
            for(ConfigurationType child : configuration.getChildrenToProcess()){
                setToSkip(child);
            }
            // Musim zrusit vypis targetElementu
            configuration.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.SKIP_PROCESS);
            // Pre istotu nastavim generovanie iba jedneho atributu, ak ide
            // o polozku nemapovanu na anotaciu resp. element
            configuration.getMappingOfConfigurationToXML().setGeneratingPolicy(GeneratingPolicy.PER_PARENT);
            configuration.setMergingPoint(false);
        } else {
            for(ConfigurationType child : configuration.getChildrenToProcess()){
                process(child);
            }
        }
        return configuration;
    }

    /**
     * Metoda nastavi vetvu na vynechanie spracovania.
     * @param configuration
     */
    private void setToSkip(ConfigurationType configuration){
        configuration.getMappingOfConfigurationToXML().setXMLOutputType(XMLProcessing.SKIP_PROCESS);
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            setToSkip(child);
        }
    }

    /**
     * Metoda najde metainformaciu Attribute medzi metainformaciami.
     * @param configuration
     * @return
     */
    private boolean attributePresent(ConfigurationType configuration){
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof AttributeType){
                return true;
            }
        }
        return false;
    }
}
