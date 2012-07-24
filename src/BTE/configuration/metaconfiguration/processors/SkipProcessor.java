package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.SkipType;

/**
 * Procesor spracuva metakonfiguraciu skip.
 * @author Milan
 */
public class SkipProcessor implements Processor {

    public ConfigurationType process(ConfigurationType configuration) {
        SkipType skip = skipPresent(configuration);
        if(skip!=null){
            // Preskoci sa co sa ma preskocit
            setToSkip(configuration, skip.getDepth());
        }
        for(ConfigurationType child : configuration.getChildrenToProcess()){
           process(child);
        }
        return configuration;
    }

    /**
     * Metoda nastavi na vynechanie vsetky polozky, ktore sa vynechat maju.
     * Rekurzivne vola seba s klesajucou hlbkou, az kym sa hlbka nevynuluje.
     * @param configuration
     * @param depth
     */
    private void setToSkip(ConfigurationType configuration, int depth){
        if(depth<0){
            return;
        }
        configuration.getMappingOfConfigurationToXML().setXMLOutputType(XMLProcessing.SKIP_PROCESS);
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            setToSkip(child, depth-1);
        }
    }

    /**
     * Najde metainformaciu Skip medzi metainformaciami.
     * @param configuration
     * @return
     */
    private SkipType skipPresent(ConfigurationType configuration){
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof SkipType){
                return (SkipType)metaConfiguration;
            }
        }
        return null;
    }
}
