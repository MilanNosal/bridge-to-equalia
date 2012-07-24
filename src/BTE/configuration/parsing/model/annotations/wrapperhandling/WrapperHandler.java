package BTE.configuration.parsing.model.annotations.wrapperhandling;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.GeneratingPolicy;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.parsing.model.annotations.ModelParsingUtilities;
import java.util.List;

/**
 * Trieda expandujuca wrappers pre generovanie podla spolocnych cielovych
 * elementov.
 * @author Milan
 */
public abstract class WrapperHandler {

    /**
     * Metoda expanduje wrappers pre informacie v modeli, ktore predstavuju
     * wrappers podla spolocnych cielovych elementov.
     * @param information
     */
    public static void populateWrappers(Information information, MetaConfigurationLoader metaconfigurationLoader){
        for(ConfigurationType configuration : information.getChildren().keySet()){
            if(areThereWrappersInBranch(configuration)){
                if(isItWrapper(configuration)){
                    if(wrapperAmongstChildren(configuration)){
                        // Ak su nasledovnici tiez wrappers, musim prv vybavit ich
                        for(Information child : information.getChildren().get(configuration)){
                            populateWrappers(child, metaconfigurationLoader);
                        }
                    }
                    // Generujem wrappers
                    List<Information> wrappers;
                    // Pre wrapper musi byt vytvorena jedina informacia, ta sa napopuluje na viac
                    Information child = information.getChildren().get(configuration).get(0);
                    if(information.getChildren().get(configuration).size()>1){
                        // Nastavim kontext podla rodica (moze byt novy)
                        child.setInformationSource(information.getInformationSource());
                        child.setTargetQualifiedName(information.getTargetQualifiedName());
                        // A na child pripojim potomkov vsetkych informacii naviac
                        for(int i = 1; i < information.getChildren().get(configuration).size(); i++){
                            // Vyberiem dalsiu nadbytocnu informaciu
                            Information nextChild = information.getChildren().get(configuration).get(i);
                            // Pre kazdy jej typ potomkov
                            for(ConfigurationType nextChildsConf : nextChild.getChildren().keySet()){
                                // Premiestnenie
                                if(child.getChildren().containsKey(nextChildsConf)){
                                    child.getChildren().get(nextChildsConf).addAll(nextChild.getChildren().get(nextChildsConf));
                                } else {
                                    child.getChildren().put(nextChildsConf, nextChild.getChildren().get(nextChildsConf));
                                }
                                for(Information moved : nextChild.getChildren().get(nextChildsConf)){
                                    moved.setParent(child);
                                }
                            }
                        }
                    }
                    
                    switch(configuration.getMappingOfConfigurationToXML().getGeneratingPolicy()){
                        // Pre kazdy typ expandovania je osobitna trieda so statickou funkciou,
                        // ktora sa postara o vyexpandovanie
                        case PER_CLASS:
                            wrappers = PerClassHandler.generateWrappersPerClass(child, metaconfigurationLoader);
                            break;
                        case PER_TOP_CLASS:
                            wrappers = PerTopClassHandler.generateWrappersPerTopClass(child, metaconfigurationLoader);
                            break;
                        case PER_TOP_PACKAGE:
                            wrappers = PerTopPackageHandler.generateWrappersPerTopPackage(child, metaconfigurationLoader);
                            break;
                        case PER_TOP_TARGET:{
                            wrappers = PerTopTargetHandler.generateWrappersPerTopTarget(child, metaconfigurationLoader);
                            break;
                        }
                        default:
                            throw new ParsingException("WrapperHandler<populateWrappers()>::\n\t" +
                                    "ERROR: I was not supposed to get here!");
                    }
                    information.getChildren().put(configuration, wrappers);
                    for(Information wrapper : wrappers){
                        wrapper.setParent(information);
                        // A napravim targets
                        fixTargetsForWrapper(wrapper);
                    }
                } else {
                    for(Information child : information.getChildren().get(configuration)){
                        populateWrappers(child, metaconfigurationLoader);
                    }
                }
            }
        }
    }

    /**
     * Opravuje mozne nekonzistencie sposobene zmenou kontextu potomkov wrapperov.
     * Tieto sa mozu vyskytnut, ak potomkovia podporuju vypis cielovych elementov
     * kontextom.
     * @param wrapper
     */
    private static void fixTargetsForWrapper(Information wrapper){
        if(wrapper.getTargetElementValue()!=null && !wrapper.getTargetElementValue().equals("")){
            fixChildsTargets(wrapper);
        }
        // Ak sa v kontexte deti wrappera nic nezmenilo, netreba nic menit
    }

    /**
     * Rekurzivna metoda starajuca sa o napravu kontextovych vypisov cielovych
     * elementov, je potrebna, ak pridanie wrappera zmeni kontext potomkov.
     * @param information
     */
    private static void fixChildsTargets(Information information){
        for(ConfigurationType configuration : information.getChildren().keySet()){
            // Najdem potomkov, ktori sa spracuvaju (taki by ani nemali existovat v modeli, ale istota je istota)
            if(configuration.getMappingOfTargetElement().getQNameOfTargetProcView()==XMLProcessing.SKIP_PROCESS){
                for(Information child : information.getChildren().get(configuration)){
                    fixChildsTargets(child);
                }
            } else {
                // Ak najdem potomkov
                switch(configuration.getMappingOfTargetElement().getQNameOfTargetProcType()){
                    // Pri kontextovom vypise opravim mapovanu informaciu o cielovom elemente
                    case CONTEXT_PRINT:{
                        for(Information child : information.getChildren().get(configuration)){
                            String newTargetValue = ModelParsingUtilities.buildTargetElementValue(information, configuration, child.getInformationSource(), child.getTargetQualifiedName());
                            child.setTargetElementValue(newTargetValue);
                        }
                        break;
                    }
                    case FULL_PRINT:
                    case SIMPLE_PRINT:{
                        // Ak mam full print, alebo simple, tie nie su nijako
                        // ovplyvnene predchadzajucimi vypismi, niet co menit
                        break;
                    }
                    default:{
                        throw new ParsingException("WrapperHandler<repairChildsTargets()>::\n\t" +
                             "ERROR: I was not supposed to get here!");
                    }
                }
                // A tym to konci, potomkovia vo vacsej hlbke nie su zmenou
                // ovplyvneni, lebo ich kontext sa nemenil
            }
        }
    }

    /**
     * Identifikuje, ci je medzi potomkami informacia typu wrapper.
     * @param configuration
     * @return
     */
    private static boolean wrapperAmongstChildren(ConfigurationType configuration){
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            if(isItWrapper(child)){
                return true;
            }
        }
        return false;
    }

    /**
     * Zisti, ci sa v danej vetve nachadzaju nejake wrappery,
     * t.j. ci ju treba spracovavat v zmysle funkcionality tejto triedy.
     * @param configuration
     * @return
     */
    public static boolean areThereWrappersInBranch(ConfigurationType configuration){
        if(isItWrapper(configuration)){
            return true;
        }
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            if(areThereWrappersInBranch(child)){
                return true;
            }
        }
        return false;
    }

    /**
     * Zisti ci je dana konfiguracia typu wrapper.
     * @param configuration
     * @return
     */
    private static boolean isItWrapper(ConfigurationType configuration){
        // Nou je, ak je sourceType == NONE a nejde o zdrojovo nemapovanu
        // informaciu typu PER_CHILD alebo PER_PARENT
        if(configuration.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS
                && configuration.getMappingOfConfigurationToSources().getSourceType()==SourceType.NONE
                && configuration.getMappingOfConfigurationToXML().getGeneratingPolicy()!=GeneratingPolicy.PER_CHILD
                && configuration.getMappingOfConfigurationToXML().getGeneratingPolicy()!=GeneratingPolicy.PER_PARENT){
            return true;
        }
        return false;
    }
}
