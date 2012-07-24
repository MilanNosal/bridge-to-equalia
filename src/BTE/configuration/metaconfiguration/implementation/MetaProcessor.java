package BTE.configuration.metaconfiguration.implementation;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.metaconfiguration.annotations.MapsTo;
import BTE.configuration.metaconfiguration.annotations.pack.BTEConfiguration;
import BTE.configuration.model.metamodel.enums.*;
import BTE.configuration.model.metamodel.implementation.ConfigurationTypeImpl;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToSources;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXSD;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXML;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfTargetElement;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import javax.lang.model.element.ElementKind;

/**
 * Procesor pre metamodel metakonfiguracie, je robeny specialne na vzhlad,
 * ktory som navrhol.
 * @author Milan
 */
public class MetaProcessor implements Processor {
    /**
     * Metametakonfiguracia.
     */
    MetaConfigurationLoader loader ;

    /**
     * Konstruktor.
     * @param loader
     */
    public MetaProcessor( MetaConfigurationLoader loader) {
        this.loader = loader;
    }

    /**
     * Pozor, skonzumuje vstupny parameter a znehodnoti ho. Pouzit treba dalej
     * navratovy objekt.
     * @param configuration
     */
    public ConfigurationType process(ConfigurationType configuration) {
        // Vezme polozku pre BTEConfiguration
        ConfigurationType BTE = getBTE(configuration);
        // a zmeni zobrazenie typu cieloveho jazykoveho elementu na "key"
        BTE.getMappingOfTargetElement().setTargetElementName("key");
        BTE.getMappingOfTargetElement().setTargetNameType(TargetNameType.USER_DEFINED);

        configuration.getChildren().remove(BTE);

        // Naklonujem si strom (bez BTE)
        ConfigurationType clone = configuration.cloneBranch();
        // Vytvorim si wrapper pre annotatcne typy
        ConfigurationType annType = generateWrapperForType();
        // a vlozim ho pod BTE
        BTE.getChildren().add(annType);
        annType.setParent(BTE);
        // K nemu pripojim polozky, ktore mozu byt aplikovane na anotacny typ
        addAnnTypes(annType, configuration, BTE);

        // Obdobne pre vlastnost
        ConfigurationType property = generateWrapperForProperty();
        annType.getChildren().add(property);
        property.setParent(annType);
        // Tu mozu ist aj niektore polozky, ktore uz boli pridane pod annType,
        // preto sa spracuvaju a pridavaju klony - kopie
        addMethodTypes(property, clone, BTE);

        configuration.getChildren().add(BTE);

        // A nakoniec este spracuje mapovanie na nazvy
        processMapsTo(configuration);
        
        return configuration;
    }

    /**
     * Metoda prehlada strom a najde konfiguraciu pre BTEConfiguration
     * @param configuration
     * @return
     */
    private ConfigurationType getBTE(ConfigurationType configuration){
        for(ConfigurationType conf : configuration.getChildren()){
            if(conf.getMappingOfConfigurationToSources().getConfAnnotation().equals(BTEConfiguration.class)){
                return conf;
            }
        }
        throw new RuntimeException("ERROR:: MetaProcessor: No Configuration for BTEConfiguration"
                + " was found.");
    }

    /**
     * Metoda prida z modelu pod annType ako potomkov polozky aplikovatelne
     * na anotacny typ.
     * @param annType
     * @param model
     * @param BTE
     */
    private void addAnnTypes(ConfigurationType annType, ConfigurationType model, ConfigurationType BTE){
        for(ConfigurationType child : model.getChildren()){
            // Vyber poloziek, ktore maju medzi cielovymi jazykovymi elementami
            // anotacny typ, pritom sa nesmiem spoliehat, ze getTargetElements
            // mi nevrati null
            if( child.getMappingOfTargetElement().getTargetElements()!=null &&
                    (Arrays.asList(child.getMappingOfTargetElement().getTargetElements()).contains(ElementType.ANNOTATION_TYPE)
                    || Arrays.asList(child.getMappingOfTargetElement().getTargetElements()).contains(ElementType.TYPE))){
                annType.getChildren().add(child);
                child.setParent(annType);
                changeTravelerAnn(child, BTE);
            }
            // Ostatne sa nepridaju
        }
        // Invalidacia modelu, pre istotu
        model.setChildren(new ArrayList<ConfigurationType>());
    }

    /**
     * Metoda prida z modelu pod propType ako potomkov polozky aplikovatelne
     * na vlasnost.
     * @param annType
     * @param model
     * @param BTE
     */
    private void addMethodTypes(ConfigurationType propType, ConfigurationType model, ConfigurationType BTE){
        for(ConfigurationType child : model.getChildren()){
            // Vyber poloziek, ktore maju medzi cielovymi jazykovymi elementami
            // metodu
            if(child.getMappingOfTargetElement().getTargetElements()!=null &&
                    Arrays.asList(child.getMappingOfTargetElement().getTargetElements()).contains(ElementType.METHOD)){
                propType.getChildren().add(child);
                child.setParent(propType);
                changeTravelerMeth(child, BTE);
            }
        }
        // Invalidacia modelu, pre istotu
        model.setChildren(new ArrayList<ConfigurationType>());
    }

    /**
     * Metoda pre zmenu niektorych poloziek pri presuvanej polozke pre anotacny
     * typ.
     * @param child
     * @param BTE
     */
    private void changeTravelerAnn(ConfigurationType child, ConfigurationType BTE){
        // Vypis cieloveho jazykoveho elementu nepotrebujem
        child.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.SKIP_PROCESS);
        // A pod annType chcem zobrazit iba metainformacie nad anotacnymi typmi
        child.getMappingOfConfigurationToSources().setSupportedSources(new ElementKind[]{ElementKind.ANNOTATION_TYPE});
        // A napokon nastavim ako kotvu element pre BTE, tak bude jedna vetva
        // s unikatnym klucom BTE (nazov baliku) obsahovat iba metainformacie
        // o mapovani anotacnych typov z daneho balika
        child.getMappingOfConfigurationToSources().setPositionAnchor(BTE);
        child.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
    }

    /**
     * Metoda pre zmenu niektorych poloziek pri presuvanej polozke pre anotacny
     * typ.
     * @param child
     * @param BTE
     */
    private void changeTravelerMeth(ConfigurationType child, ConfigurationType BTE){
        // Vypis cieloveho jazykoveho elementu nepotrebujem
        child.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.SKIP_PROCESS);
        // A pod property chcem zobrazit iba metainformacie nad vlastnostami
        child.getMappingOfConfigurationToSources().setSupportedSources(new ElementKind[]{ElementKind.METHOD});
        // A kotva na BTE
        child.getMappingOfConfigurationToSources().setPositionAnchor(BTE);
        child.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
    }

    /**
     * Metoda vygeneruje polozku pre wrapper pre vlastnosti.
     * @return
     */
    private ConfigurationType generateWrapperForProperty(){
        // Dam si kontextovy vypis cieloveho jazykoveho elementu
        MappingOfTargetElement target = new MappingOfTargetElement(
                QNameProcessing.CONTEXT_PRINT, XMLProcessing.ATTRIBUTE,
                null, TargetNameType.GENERIC, new ElementType[]{ElementType.METHOD});
        // Vzhlad - mapovanie na XML
        MappingOfConfigurationToXML view = new MappingOfConfigurationToXML("declMethod", "${name}-type",
                null, XMLProcessing.ELEMENT, 0, 0, -1);
        // Pre kazdu metodu (rovnaky vysledok pre PER_TARGET v tomto pripade)
        view.setGeneratingPolicy(GeneratingPolicy.PER_TOP_TARGET);
        
        MappingOfConfigurationToXSD type = new MappingOfConfigurationToXSD(TypeOfElement.NONE, null);
        MappingOfConfigurationToSources source = new MappingOfConfigurationToSources(null, "", SourceType.NONE);
        
        ConfigurationType wrapper = new ConfigurationTypeImpl(type, source, view, target, null);
        wrapper.setMergingPoint(true);
        
        return wrapper;
    }

    /**
     * Metoda vygeneruje polozku pre wrapper pre annotacne typy.
     * @return
     */
    private ConfigurationType generateWrapperForType(){
        MappingOfTargetElement target = new MappingOfTargetElement(QNameProcessing.CONTEXT_PRINT, XMLProcessing.ATTRIBUTE,
                null, TargetNameType.GENERIC,
                new ElementType[]{ElementType.ANNOTATION_TYPE});
        MappingOfConfigurationToXML view = new MappingOfConfigurationToXML("annotationType", "${name}-type",
                null, XMLProcessing.ELEMENT, 0, 0, -1);
        // Pre kazdu triedu - teda anotacny typ
        view.setGeneratingPolicy(GeneratingPolicy.PER_TOP_CLASS);

        MappingOfConfigurationToXSD type = new MappingOfConfigurationToXSD(TypeOfElement.NONE, null);

        MappingOfConfigurationToSources source = new MappingOfConfigurationToSources(null, "", SourceType.NONE);
        ConfigurationType wrapper = new ConfigurationTypeImpl(type, source, view, target, null);
        wrapper.setMergingPoint(true);

        return wrapper;
    }

    /**
     * Metoda pre spracovanie mapovania nazvov.
     * @param model
     */
    private void processMapsTo(ConfigurationType model){
        MapsTo mt = findMapsTo(model);
        if(mt!=null){
            // Ak sa najde anotacia MapsTo tak sa podla nej zmeni nazov
            model.getMappingOfConfigurationToXML().setName(mt.name());
            model.getMappingOfConfigurationToXML().setTypeName(mt.typeName());
        }
        for(ConfigurationType child : model.getChildren())
            processMapsTo(child);
    }

    /**
     * Metoda prehlada metainformacie pri polozke a hlada anotaciu MapsTo.
     * @param configuration
     * @return
     */
    private MapsTo findMapsTo(ConfigurationType configuration){
        for(Object object : configuration.getMetainformations()){
            if(object instanceof MapsTo){
                return (MapsTo)object;
            }
        }
        return null;
    }
}
