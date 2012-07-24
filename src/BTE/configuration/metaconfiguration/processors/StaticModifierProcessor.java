package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.*;
import BTE.configuration.model.metamodel.extractors.StaticModifierExtractor;
import BTE.configuration.model.metamodel.implementation.ConfigurationTypeImpl;
import BTE.configuration.model.metamodel.implementation.properties.*;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.StaticModifierType;

/**
 * Procesor na spracovanie metakonfiguracie staticModifier. Tato metakonfiguracia
 * nema najeky valny vyznam, islo o prezentaciu moznosti rozhrania ValueExtractor.
 * @author Milan
 */
public class StaticModifierProcessor implements Processor {

    public ConfigurationType process(ConfigurationType configuration) {
        StaticModifierType metaConfiguration = staticPresent(configuration);
        if(metaConfiguration!=null){
            // Vynechanie vypisu cieloveho jazykoveho elementu
            MappingOfTargetElement target = new MappingOfTargetElement();

            // Vzhlad - element/atribut s nazvom static
            MappingOfConfigurationToXML view = new MappingOfConfigurationToXML("static", "${name}-type",
                null,
                metaConfiguration.isElement()?XMLProcessing.ELEMENT:XMLProcessing.ATTRIBUTE,
                0, 0, 1);
            // Typ jednoduchy, s xsd:boolean typom
            MappingOfConfigurationToXSD type = new MappingOfConfigurationToXSD(TypeOfElement.VALUE, new String[]{boolean.class.getName()});
            // Vlastny zdroj
            MappingOfConfigurationToSources source = new MappingOfConfigurationToSources(null, "", SourceType.USER_DEFINED, RelativePositionToAnchor.NONE, configuration);
            // S takymto ValueExtractor-om
            source.setInformationExtractor(new StaticModifierExtractor());
            ConfigurationType statMod = new ConfigurationTypeImpl(type, source, view, target, configuration);
            configuration.getChildren().add(statMod);
        }
        for(ConfigurationType child : configuration.getChildren()){
            process(child);
        }
        return configuration;
    }

    /**
     * Hlada vyskyt metainformacie StaticModifier.
     * @param configuration
     * @return
     */
    private StaticModifierType staticPresent(ConfigurationType configuration){
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof StaticModifierType){
                return (StaticModifierType)metaConfiguration;
            }
        }
        return null;
    }
}
