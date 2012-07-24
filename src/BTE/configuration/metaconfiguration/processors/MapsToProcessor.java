package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.MapsToType;

/**
 * Spracovanie metakonfiguracie MapsTo.
 * @author Milan
 */
public class MapsToProcessor implements Processor {

    public ConfigurationType process(ConfigurationType configuration) {
        MapsToType metaConfiguration = mapsToPresent(configuration);
        if(metaConfiguration!=null){
            // Zmena nazvu podla MapsTo
            configuration.getMappingOfConfigurationToXML().setName(metaConfiguration.getName());
            configuration.getMappingOfConfigurationToXML().setTypeName(metaConfiguration.getTypeName());
        }
        for(ConfigurationType child : configuration.getChildren()){
            process(child);
        }
        return configuration;
    }

    /**
     * Najde vyskyt metainformacie MapsTo medzi metainformaciami.
     * @param configuration
     * @return
     */
    private MapsToType mapsToPresent(ConfigurationType configuration) {
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof MapsToType){
                return (MapsToType)metaConfiguration;
            }
        }
        return null;
    }

}
