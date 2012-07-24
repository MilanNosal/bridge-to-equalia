package BTE.configuration.parsing.model.combining;

import BTE.configuration.model.metamodel.enums.GeneratingPolicy;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import java.util.ArrayList;
import java.util.List;

/**
 * Trieda poskytuje metody na expanziu a kontrakciu nemapovanych informacii, ktore
 * sa populuju s politikou PER_CHILD. Pri spajani je totiz omnoho pohodlnejsie
 * porovnavat informacie, ak su informacie tohto typu spojene do jedneho.
 * @author Milan
 */
public class PerChildHandler {
    /**
     * Nahradi vyskyty viacerych informacii jednou tam, kde je nastavene generovanie
     * PER_CHILD - kontrakcia.
     * @param information
     */
    public static void contractInformationPerChild(Information information){
        for(ConfigurationType configuration : information.getChildren().keySet()){
            // V kazdom type potomkov sa pozriem, ci nejde o typ pri ktorom
            // ma ku kontrakcii dojst
            if(configuration.getMappingOfConfigurationToSources().getSourceType() == SourceType.NONE
                    && configuration.getMappingOfConfigurationToXML().getGeneratingPolicy() == GeneratingPolicy.PER_CHILD
                    && configuration.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS){
                if(information.getChildren().get(configuration).isEmpty())
                    continue;
                // Vygenerujem jednu novu informaciu podla prvej (vsetky tohto typu by mali byt rovnake)
                Information example = information.getChildren().get(configuration).get(0);
                Information newChild = new InformationImpl(example.getTargetQualifiedName(), example.getTargetElementValue(),
                        example.getTargetElementName(), example.getValue(), configuration);
                // Zoznam pre potomkov
                List<Information> list;
                // Teraz musim pospajat nasledovnikov spajanych informacii
                for(Information child : information.getChildren().get(configuration)){
                    // Pre kazdu informaciu
                    for(ConfigurationType childConfiguration : child.getChildren().keySet()){
                        // A kazdy typ jej potomkov, ich pridam novemu rodicovi - informacii newChild
                        if(newChild.getChildren().containsKey(childConfiguration)){
                            for(Information grandChild : child.getChildren().get(childConfiguration)){
                                grandChild.setParent(newChild);
                                newChild.getChildren().get(childConfiguration).add(grandChild);
                            }
                        } else {
                            // Ak takyto typ este nie je v tabulke potomkov newChild,
                            // tak pridam novy zaznam do tabulky
                            list = new ArrayList<Information>();
                            for(Information grandChild : child.getChildren().get(childConfiguration)){
                                list.add(grandChild);
                                grandChild.setParent(newChild);
                            }
                            newChild.getChildren().put(childConfiguration, list);
                        }
                    }
                }
                // A napokon pridam noveho potomka namiesto tych kontraktovanych
                list = new ArrayList<Information>();
                list.add(newChild);
                // A vykonam nahradu
                information.getChildren().put(configuration, list);
                newChild.setParent(information);
            } else if(isPerChildInBranch(configuration)) {
                // Inak testujem ci vobec ma zmysel ist viac do hlbky
                for(Information child : information.getChildren().get(configuration)){
                    contractInformationPerChild(child);
                }
            }
        }
    }

    /**
     * Nahradi vyskyty jednej informacie mnohymi tam, kde je nastavene generovanie
     * PER_CHILD - expanzia.
     * @param information
     */
    public static void expandInformationPerChild(Information information){
        for(ConfigurationType configuration : information.getChildren().keySet()){
            // Obdobne ako pri kontrakcii prechadzam vsetky typy potomkov
            if(configuration.getMappingOfConfigurationToSources().getSourceType() == SourceType.NONE
                    && configuration.getMappingOfConfigurationToXML().getGeneratingPolicy() == GeneratingPolicy.PER_CHILD
                    && configuration.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS){
                // Ak ide o expandovany typ
                if(information.getChildren().get(configuration).isEmpty())
                    continue;
                // Vyberiem prvy prvok (mal by byt iba jediny, prave ten, ktory chcem expandovat)
                Information example = information.getChildren().get(configuration).get(0);
                // Odstranim ho spomedzi potomkov
                information.getChildren().get(configuration).remove(example);
                // Do tohto zoznamu budem pridavat expandovane informacie
                List<Information> list = information.getChildren().get(configuration);
                // Pomocna premenna pre potomkov expandovanych informacii
                List<Information> tempList;

                // Najprv musim expandovat nizsie urovne, aby som dostal
                // spravny vysledok
                expandInformationPerChild(example);
                for(ConfigurationType grandChildConfiguration : example.getChildren().keySet()){
                    for(Information grandChild : example.getChildren().get(grandChildConfiguration)){
                        // A pre kazdeho potomka expandovanej informacie vytvorim
                        // noveho rodica
                        Information newChild = new InformationImpl(example.getTargetQualifiedName(), example.getTargetElementValue(),
                            example.getTargetElementName(), example.getValue(), configuration);
                        tempList = new ArrayList<Information>();
                        tempList.add(grandChild);
                        grandChild.setParent(newChild);
                        newChild.getChildren().put(grandChildConfiguration, tempList);
                        list.add(newChild);
                        newChild.setParent(information);
                    }
                }
                
            } else if(isPerChildInBranch(configuration)) {
                // A opat testujem ci ma vobec zmysel pokracovat v expanzii
                for(Information child : information.getChildren().get(configuration)){
                    expandInformationPerChild(child);
                }
            }
        }
    }


    /**
     * Zisti, ci sa vo vetve nachadza nejaka konfiguracia, ktora ma politiku
     * generovania PER_CHILD.
     * @param configuration
     * @return
     */
    public static boolean isPerChildInBranch(ConfigurationType configuration){
        if(configuration.getMappingOfConfigurationToSources().getSourceType() == SourceType.NONE
                && configuration.getMappingOfConfigurationToXML().getGeneratingPolicy() == GeneratingPolicy.PER_CHILD
                && configuration.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS){
            return true;
        }
        for(ConfigurationType child : configuration.getChildrenToProcess()){
            if(isPerChildInBranch(child)){
                return true;
            }
        }
        return false;
    }
}
