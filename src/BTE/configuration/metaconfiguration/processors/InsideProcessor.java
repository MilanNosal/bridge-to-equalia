package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.enums.RelativePositionToAnchor;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import java.util.ArrayList;
import java.util.List;
import sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration.InsideType;

/**
 * Spracovanie metakonfiguracie Inside. Stara sa o preusporiadanie hierarchie.
 * @author Milan
 */
public class InsideProcessor implements Processor {

    public ConfigurationType process(ConfigurationType configuration) {
        // Spracujem Inside metainformacie
        processInside(configuration, configuration);
        return configuration;
    }

    /**
     * Vlastna metoda pre spracovanie metainformacie Inside. Preberam dva
     * argumenty, jednym je prave spracovavana polozka, druhym je korenova
     * polozka, teda vlastne samotny model. Pouzivam ho na hladanie rodica v
     * novom vztahu rodic-potomok.
     * @param configuration
     * @param model
     */
    private void processInside(ConfigurationType configuration, ConfigurationType model) {
        // Najdem metainformaciu
        InsideType metaConfiguration = insidePresent(configuration);
        // inside je polozka, ktora sa stane potomkom v novom vztahu
        ConfigurationType inside = configuration;
        // Spracuvam metainformaciu ak tu je a nema priznak spracovania
        // (aby som predisiel viacnasobnemu spracovaniu)
        if(metaConfiguration!=null && !metaConfiguration.isProcessed()){
            // Odstranime premiestnovanu polozku z metamodelu (ktory bude
            // prehladavany), aby sa nestala situacia, ze premiestnovana polozka
            // sa bude premiestnovat niekde do vlastnej vetvy - co by bolo
            // nezmyselne
            inside.getParent().getChildren().remove(inside);
            // Pokusime sa najst rodica
            ConfigurationType newParent = findConfiguration(model, metaConfiguration.getParent());
            if(newParent==null){
                // Ak rodic najdeny nebol, tak napravime co sme "napachali" a
                // pojdeme na dalsiu polozku
                inside.getParent().getChildren().add(inside);
                metaConfiguration.setProcessed(true);
            } else {
                // Musim pripravit aj rodica na prijatie deti :)
                // Rodic evidentne nemoze byt atribut
                if(newParent.getMappingOfConfigurationToXML().getXMLOutputType()==XMLProcessing.ATTRIBUTE){
                    throw new RuntimeException("BTE.InsideProcessor::\n\tERROR: Misuse of Inside metaconfiguration.");
                }
                // Podla typu zdroja informacii k polozke
                switch(inside.getMappingOfConfigurationToSources().getSourceType()){
                    case ANNOTATION:{
                        // Ak ide o anotacny typ
                        inside.setParent(newParent);
                        newParent.getChildren().add(inside);
                        // Tak proste ako kotvu uvedieme noveho rodica
                        inside.getMappingOfConfigurationToSources().setPositionAnchor(newParent);
                        // Priznak spajania a politiku hladania uvedieme podla
                        // metainformacie
                        inside.setMergingPoint(metaConfiguration.isSetMergingPoint());
                        switch(metaConfiguration.getLevel()){
                            case HIGHER:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.HIGHER_LVL);
                                break;
                            case LOWER:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.LOWER_LVL);
                                break;
                            case SAME:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LVL);
                                break;
                            case SAME_HIGHER:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_HIGHER_LVL);
                                break;
                            case SAME_LOWER:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
                                break;
                            default:
                                inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
                                break;
                        }
                        // Odfajkneme ako spracovanu
                        metaConfiguration.setProcessed(true);
                        break;
                    }
                    case DECL_PROP_ANNOTATION:
                    case DECL_PROP_ARRAY_ANNOTATION:
                    case DECL_PROP_ARRAY_ENUM:
                    case DECL_PROP_ARRAY_PRIMITIVE:
                    case DECL_PROP_ARRAY_STRING:
                    case DECL_PROP_ENUM:
                    case DECL_PROP_PRIMITIVE:
                    case DECL_PROP_STRING:{
                        // Ak je vsak polozka pre posun vlastnostou, je
                        // potrebne najst anotaciu, ktorej patri, a tu presunut
                        // (jej kopiu). Kopii sa da priznak SKIP_PROCESS, cim
                        // sa mapovat nebude, a dosiahneme tym efekt, akoby sa
                        // presunula iba vlastnost
                        ConfigurationType substitute = getSubstitute(inside);
                        if(substitute==null){
                            // Ak nevieme najst jej anotaciu, tak opat akciu
                            // rusime
                            inside.getParent().getChildren().add(inside);
                            metaConfiguration.setProcessed(true);
                        } else {
                            // Zmenime este pocet vyskytov na lubovolne vela,
                            // podla jej anotacie, tych moze byt mnozstvo
                            // Vyznam obmezdovat na 1 by malo pri urovni
                            // hladania SAME_LVL, ale az tak sa s tym nepiplam
                            inside.getMappingOfConfigurationToXML().setMinOccurs(0);
                            inside.getMappingOfConfigurationToXML().setMaxOccurs(-1);
                            // A este target processing predpokladam ze je ziadany
                            inside.getMappingOfTargetElement().setQNameOfTargetProcView(XMLProcessing.ATTRIBUTE);
                            // Nastavim nove vztahy
                            substitute.setParent(newParent);
                            newParent.getChildren().add(substitute);
                            substitute.getMappingOfConfigurationToSources().setPositionAnchor(newParent);
                            // A spajanie a politiku hladania podla metainformacie
                            substitute.setMergingPoint(metaConfiguration.isSetMergingPoint());

                            switch(metaConfiguration.getLevel()){
                                case HIGHER:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.HIGHER_LVL);
                                    break;
                                case LOWER:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.LOWER_LVL);
                                    break;
                                case SAME:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LVL);
                                    break;
                                case SAME_HIGHER:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_HIGHER_LVL);
                                    break;
                                case SAME_LOWER:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
                                    break;
                                default:
                                    inside.getMappingOfConfigurationToSources().setRelPositionToAnchor(RelativePositionToAnchor.SAME_LOWER_LVL);
                                    break;
                            }
                            
                            metaConfiguration.setProcessed(true);
                        }
                        break;
                    }
                    default:{
                        throw new RuntimeException("BTE.InsideProcessor::\n\tERROR: Misuse of Inside metaconfiguration.");
                    }
                }
            }
        }
        // A spracujem potomkov.. novy list, pretoze list s potomkami sa moze
        // v priebehu spracovania menit
        List<ConfigurationType> list = new ArrayList<ConfigurationType>();
        list.addAll(inside.getChildren());
        for(ConfigurationType child : list){
            processInside(child, model);
        }
    }

    /**
     * Metoda sa pokusi najst pre danu polozku polozku odpovedajucu anotacii.
     * Predpoklada sa, ze argument je polozka so zdrojom typu deklarovanej
     * vlastnosti. Popri hladani sa orezu vsetky nadbytocne polozky vo vetve,
     * ostane iba cesta k zamienanej polozke, teda ku configuration.
     * @param configuration
     * @return
     */
    private ConfigurationType getSubstitute(ConfigurationType configuration){
        // Pripravim si pomocne premenne
        // Ret vetvu, ktora sa ma prenasat v hierarchii
        ConfigurationType ret = configuration;
        ConfigurationType temp2;
        String configurationName = configuration.getMappingOfConfigurationToXML().getName();
        // Temp mi ponesie referenciu na kotvu, tj na anotaciu
        ConfigurationType temp = configuration.getMappingOfConfigurationToSources().getPositionAnchor();
        // configuration referenciu na aktualneho rodica, budem sa snazit
        // prejst po rodicoch az ku kotve
        configuration=configuration.getParent();
        // Pokial je rodic
        while(configuration!=null){
            // vytvorim si klon rodica, ktory bude v novej vetve
            temp2 = configuration.clone();
            // Nastavim aby sa nemapoval
            temp2.getMappingOfConfigurationToXML().setXMLOutputType(XMLProcessing.SKIP_PROCESS);

            // Aby som nemal problem s vkladanim do tabuliek, musim rozlisit novy typ
            // (pripad ze vlastnost anotacie k rodicovi anotacie - mal by som
            // dvakrat MM typ pre anotaciu, jeden by sa sice preskakoval, ale pri
            // spracovani by mi jeden typ prekryl druhy)
            // Este predtym sa vsak uistim, ze cely podstrom bude vediet o zmene
            fixDependingTypes(ret, temp2);
            temp2.getMappingOfConfigurationToXML().setName(temp2.getMappingOfConfigurationToXML().getName()+configurationName);
            temp2.getMappingOfConfigurationToXML().setTypeName(temp2.getMappingOfConfigurationToXML().getTypeName()+configurationName);
            
            // A bude mat jedineho potomka, a tym je prenasana vetva (zlozena
            // z vetvy definovanej polozkou, ktora sa ma preniest, a kopie cesty
            // od polozky k vyskytu anotacie, ktorej patri
            temp2.getChildren().add(ret);
            ret.setParent(temp2);
            // Po pridani sa upravi rodic
            ret = temp2;

            // Ak sme sa cez rodicov dostali az ku kotve, tj anotacii
            if(configuration.equals(temp)){
                switch(configuration.getMappingOfConfigurationToSources().getSourceType()){
                    case ANNOTATION:{
                        // a ta anotacia uz nie je vlastnostou inej, mozeme
                        // hladanie uspesne ukoncit
                        return ret;
                    }
                    case DECL_PROP_ANNOTATION:
                    case DECL_PROP_ARRAY_ANNOTATION:
                    case DECL_PROP_ARRAY_ENUM:
                    case DECL_PROP_ARRAY_PRIMITIVE:
                    case DECL_PROP_ARRAY_STRING:
                    case DECL_PROP_ENUM:
                    case DECL_PROP_PRIMITIVE:
                    case DECL_PROP_STRING:{
                        // ak vsak najdena kotva je zasa iba vlastnostou inej
                        // anotacie, tak nastavim hladanu kotvu na kotvu
                        // najdenej polozky a hladanie pokracuje
                        temp = temp.getMappingOfConfigurationToSources().getPositionAnchor();
                        break;
                    }
                    default:{
                        throw new ParsingException("InsideProcessor::\n ERROR:\t"
                                + "Inconsistency in metamodel, some ConfigurationType object is declared to be"
                                + " declared property of other ConfigurationType, but its anchor is not among"
                                + " its ancestors.");
                    }
                }
            }
            // Posuniem sa o rodica vyssie
            configuration = configuration.getParent();
        }
        // No a ak nenajdem
        throw new ParsingException("InsideProcessor::\n ERROR:\t"
              + "Inconsistency in metamodel, some ConfigurationType object is declared to be"
              + " declared property of other ConfigurationType, but its anchor is not among"
              + " its ancestors.");
    }

    /**
     * Metoda hlada polozku s danym menom v strome metamodelu.
     * @param configuration
     * @param elementName
     * @return
     */
    private ConfigurationType findConfiguration(ConfigurationType configuration, String elementName){
        if(!configuration.isCloned() && configuration.getMappingOfConfigurationToXML().getName().equals(elementName)){
            return configuration;
        }
        for(ConfigurationType child : configuration.getChildren()){
            // Rekurzivne hladanie v strome metamodelu
            ConfigurationType found = findConfiguration(child, elementName);
            if(found!=null){
                return found;
            }
        }
        return null;
    }

    /**
     * Metoda najde metainformaciu medzi metainformaciami pri polozke.
     * @param configuration
     * @return
     */
    private InsideType insidePresent(ConfigurationType configuration){
        for(Object metaConfiguration : configuration.getMetainformations()){
            if(metaConfiguration instanceof InsideType){
                return (InsideType)metaConfiguration;
            }
        }
        return null;
    }

    /**
     * Cielom metody je prejst podstrom typu root a najst vsetky take typy,
     * ktore zavisia vztahom anchor (t.j. sa nan odkazuju) s typom depender.
     * Porovnavanie sa vykonava na zaklade metody equals, ktora vracia true
     * aj v pripade ze depender je clone, v pripade uspechu sa anchor nastavi
     * na depender, tymto chcem zabezpecit, ze ak zmenim nejako typ depender,
     * zmenia sa aj vsetky odkazy nan. Zmena depender-a ma zmysel v pripade
     * klonovania, ak nechcem aby sa mi bili odkazy na klon a na original.
     * @param root
     * @param depender
     */
    private void fixDependingTypes(ConfigurationType root, ConfigurationType depender){
        if(root.getMappingOfConfigurationToSources().getPositionAnchor()!=null
                && root.getMappingOfConfigurationToSources().getPositionAnchor().equals(depender)){
            root.getMappingOfConfigurationToSources().setPositionAnchor(depender);
        }
        for(ConfigurationType child : root.getChildren()){
            fixDependingTypes(child, depender);
        }
    }
}
