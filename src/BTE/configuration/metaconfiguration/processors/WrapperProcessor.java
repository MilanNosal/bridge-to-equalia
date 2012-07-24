package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.GeneratingPolicy;
import BTE.configuration.model.metamodel.enums.QNameProcessing;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.enums.TypeOfElement;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.implementation.ConfigurationTypeImpl;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToSources;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXSD;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXML;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfTargetElement;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.WrapperType;

/**
 * Spracuva wrapper metakonfiguraciu.
 * @author Milan
 */
public class WrapperProcessor implements Processor {
    public ConfigurationType process(ConfigurationType configuration) {
        // Najdenie wrapper metainformacie
        WrapperType metaConfiguration = wrapperPresent(configuration);
        if(metaConfiguration!=null){            
            // Vygenerujem prislusny wrapper
            ConfigurationType wrapper = generateWrapper(metaConfiguration);
            if(metaConfiguration.isChildWrapping()){
                // Ak nim chcem oddelit potomkov, tak ho vlozim medzi
                // aktualnu polozku a vybranych potomkov
                List<ConfigurationType> wrapped = getChildrenToWrap(metaConfiguration, configuration);
                wrapper.getChildren().addAll(wrapped);
                for(ConfigurationType child : wrapper.getChildren()){
                    child.setParent(wrapper);
                    child.getMappingOfConfigurationToXML().setMinOccurs(0);
                }
                configuration.getChildren().add(wrapper);
                wrapper.setParent(configuration);
            } else {
                // Inak skusim na rodica aplikovat rovnaku politiku
                ConfigurationType parent = configuration.getParent();
                if(parent==null){
                    configuration.setParent(wrapper);
                    wrapper.getChildren().add(configuration);
                } else {
                    List<ConfigurationType> wrapped = getChildrenToWrap(metaConfiguration, parent);
                    wrapper.getChildren().addAll(wrapped);
                    for(ConfigurationType child : wrapper.getChildren()){
                        child.setParent(wrapper);
                        child.getMappingOfConfigurationToXML().setMinOccurs(0);
                    }
                    parent.getChildren().add(wrapper);
                    wrapper.setParent(parent);
                }
            }
        }
        // Musim vytvarat novy zoznam, pretoze ten s potomkami sa moze pri
        // spracovani menit
        List<ConfigurationType> list = new ArrayList<ConfigurationType>();
        list.addAll(configuration.getChildren());
        for(ConfigurationType child : list){
            process(child);
        }
        return configuration;
    }

    /**
     * Metoda vyhlada zo zoznamu potomkov tych, ktori boli vybrani.
     * @param wrapper
     * @param parent
     * @return
     */
    private List<ConfigurationType> getChildrenToWrap(WrapperType wrapper, ConfigurationType parent){
        List<ConfigurationType> list = new ArrayList<ConfigurationType>();
        if(wrapper.getWrappedElements()!=null && !wrapper.getWrappedElements().isEmpty()){
            // Ak nie je zoznam prazdny, tak podla mena vyberiem z potomkov tych,
            // ktorych treba premiestnit
            for(ConfigurationType child : parent.getChildren()){
                if(wrapper.getWrappedElements().contains(child.getMappingOfConfigurationToXML().getName())){
                    list.add(child);
                }
            }
        } else {
            // Inak beriem vsetkych potomkov
            list.addAll(parent.getChildren());
        }
        // A od rodica vynulujem vztah, aby som na to nezabudol
        for(ConfigurationType child : list){
            parent.getChildren().remove(child);
            child.setParent(null);
        }
        return list;
    }

    /**
     * Metoda vygeneruje ziadany wrapper.
     * @param wrapperInfo
     * @return
     */
    private ConfigurationType generateWrapper(WrapperType wrapperInfo){
        ElementType[] names;
        // Podla vybranej politiky grupovania vyberiem genericke mena typov
        // jazykovych elementov
        switch(wrapperInfo.getTargetCoupling()){
            case PACKAGE:
            case TOP_PACKAGE:
                names = new ElementType[]{ElementType.PACKAGE};
                break;
            case CLASS:
            case TOP_CLASS:
                names = new ElementType[]{ElementType.TYPE};
                break;
            default:
                names = ElementType.values();
                break;
        }

        // a nastavim kontextovy vypis
        MappingOfTargetElement target = new MappingOfTargetElement(QNameProcessing.CONTEXT_PRINT, XMLProcessing.ATTRIBUTE,
                null, TargetNameType.GENERIC, names);
        
        // Vzhlad urcim podla metainformacie
        MappingOfConfigurationToXML view = new MappingOfConfigurationToXML(wrapperInfo.getWrapperName(), wrapperInfo.getTypeName(),
                null, XMLProcessing.ELEMENT, 0, 0, -1);
        switch(wrapperInfo.getTargetCoupling()){
            case PACKAGE:
            case TOP_PACKAGE:
                view.setGeneratingPolicy(GeneratingPolicy.PER_TOP_PACKAGE);
                break;
            case CLASS:
                view.setGeneratingPolicy(GeneratingPolicy.PER_CLASS);
                break;
            case TOP_CLASS:
                view.setGeneratingPolicy(GeneratingPolicy.PER_TOP_CLASS);
                break;
            case TARGET:
            case TOP_TARGET:
                view.setGeneratingPolicy(GeneratingPolicy.PER_TOP_TARGET);
                break;
            case ONE:
                view.setGeneratingPolicy(GeneratingPolicy.PER_PARENT);
                break;
            case PER_CHILD:
                view.setGeneratingPolicy(GeneratingPolicy.PER_CHILD);
                break;
            default:
                view.setGeneratingPolicy(GeneratingPolicy.PER_PARENT);
                break;
        }
        
        MappingOfConfigurationToXSD type = new MappingOfConfigurationToXSD(TypeOfElement.NONE, null);
        MappingOfConfigurationToSources source = new MappingOfConfigurationToSources(null, "", SourceType.NONE);
        ConfigurationType wrapper = new ConfigurationTypeImpl(type, source, view, target, null);
        
        wrapper.setMergingPoint(true);
        return wrapper;
    }

    /**
     * Metoda najde metainformaciu o wrapper na danej polozke.
     * @param configuration
     * @return
     */
    private WrapperType wrapperPresent(ConfigurationType configuration) {
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof WrapperType){
                return (WrapperType)metaConfiguration;
            }
        }
        return null;
    }
}
