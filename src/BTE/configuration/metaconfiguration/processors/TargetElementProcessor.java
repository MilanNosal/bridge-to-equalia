package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.QNameProcessing;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.TargetElementType;

/**
 * Procesor spracuvajuci metakonfiguraciu TargetElement.
 * @author Milan
 */
public class TargetElementProcessor implements Processor {

    public ConfigurationType process(ConfigurationType configuration) {
        TargetElementType metaConfiguration = targetElementPresent(configuration);
        ConfigurationType changing = null;
        if(metaConfiguration != null){
            // Skusam, ci sa nenajde iny konf. typ, ku ktoremu patri tato metainformacia
            if(!metaConfiguration.getTargetConfiguration().equals("")){
                changing = findTargetConfiguration(configuration, metaConfiguration.getTargetConfiguration());
            } else {
                changing = configuration;
            }
            if(changing!=null){
                // Ak metainformacie existuje, spracujem
                // Zmena nazvu pre targetElement - cielovy jazykovy element
                if(metaConfiguration.isGeneric()){
                    changing.getMappingOfTargetElement().setTargetNameType(TargetNameType.GENERIC);
                } else {
                    changing.getMappingOfTargetElement().setTargetNameType(TargetNameType.USER_DEFINED);
                    changing.getMappingOfTargetElement().setTargetElementName(metaConfiguration.getName());
                }

                // Podla priznaku isElement sa rozhodne ci sa bude mapovat na
                // atribut alebo element
                if(metaConfiguration.isElement()){
                    changing.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.ELEMENT);
                } else {
                    changing.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.ATTRIBUTE);
                }

                switch(metaConfiguration.getPrintType()){
                    case CONTEXT:
                        changing.getMappingOfTargetElement().setQNameOfTargetProcType(QNameProcessing.CONTEXT_PRINT);
                        break;
                    case FULL:
                        changing.getMappingOfTargetElement().setQNameOfTargetProcType(QNameProcessing.FULL_PRINT);
                        break;
                    case NO_PRINT:
                        changing.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.SKIP_PROCESS);
                        break;
                    case SIMPLE:
                        changing.getMappingOfTargetElement().setQNameOfTargetProcType(QNameProcessing.SIMPLE_PRINT);
                        break;
                    default:
                        changing.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.SKIP_PROCESS);
                        break;
                }
            }

        }
        for(ConfigurationType child : configuration.getChildren()){
            process(child);
        }
        return configuration;
    }

    /**
     * Metoda hlada konfiguracny typ s mapovanim do XML s menom name na ceste
     * ku korenu a medzi potomkami configuration.
     * @param configuration
     * @param name
     * @return
     */
    private ConfigurationType findTargetConfiguration(ConfigurationType configuration, String name){
        ConfigurationType parent = configuration.getParent();
        while(parent!=null){
            if(parent.getMappingOfConfigurationToXML().getName().equals(name)){
                return parent;
            }
            parent = parent.getParent();
        }
        return scanChildren(configuration, name);
    }

    /**
     * Metoda prehlada potomkov configuration s cielom najst konf. typ s menom
     * mapovania do XML name.
     * @param configuration
     * @param name
     * @return
     */
    private ConfigurationType scanChildren(ConfigurationType configuration, String name){
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            if(child.getMappingOfConfigurationToXML().getName().equals(name)){
                return child;
            }
            ConfigurationType found = scanChildren(child, name);
            if(found!=null)
                return found;
        }
        return null;
    }

    /**
     * Metoda najde vyskyt metainformacie TargetElement.
     * @param configuration
     * @return
     */
    private TargetElementType targetElementPresent(ConfigurationType configuration) {
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof TargetElementType){
                return (TargetElementType)metaConfiguration;
            }
        }
        return null;
    }
}
