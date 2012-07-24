package BTE.configuration.parsing.model.combining;

import BTE.configuration.communication.Priority;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trieda sa ma postarat o spajanie dvoch modelov.
 * @author Milan
 */
public class ModelCombiner {
    // Vysledny model po spajani
    private Information model;

    /**
     * Konstruktor vytvori novy objekt triedy ModelMerger a spoji modely,
     * ktore prebera ako argumenty. Modely su po spajani <b>modifikovane</b>
     * - invalidovane.
     * @param annotations model z anotacii
     * @param xml model z xml
     * @param priority priorita spajania
     */
    public ModelCombiner(Information annotations, Information xml, Priority priority) {
        if(annotations!=null && xml!=null){
            if(!annotations.getMMConfiguration().equals(xml.getMMConfiguration())){
                // Musi ist o rovnake typy modelov
                throw new ParsingException("ModelMerger<Constructor>::\n\t"+
                        "ERROR: Arguments \"annotations\" and \"xml\" must be equivalent," +
                        " they have to share same MetaModelConfiguration.");
            }
            if(PerChildHandler.isPerChildInBranch(annotations.getMMConfiguration())){
                // Pre potreby spajania nemapovane informacie s politikou
                // generovania PER_CHILD spojim do jednej informacie
                PerChildHandler.contractInformationPerChild(annotations);
                PerChildHandler.contractInformationPerChild(xml);
            }
            // Podla priority volim prioritny model a spajam
            if(priority == Priority.XML){
                model = xml;
                combineInformation(xml, annotations, model);
            } else {
                model = annotations;
                combineInformation(annotations, xml, model);
            }
            if(PerChildHandler.isPerChildInBranch(model.getMMConfiguration())){
                // A znova expandujem informacie s politikou PER_CHILD
                PerChildHandler.expandInformationPerChild(model);
            }
        } // Ak je uvedeny iba jeden model, vratim rovno druhy
        else if(annotations == null && xml != null) {
            model = xml;
        } else if(annotations != null && xml == null){
            model = annotations;
        } // Ak nie je uvedeny ziadny vraciam null
        else {
            model = null;
        }
    }

    /**
     * Konstruktor vytvori novy objekt triedy ModelMerger a spoji modely,
     * ktore prebera ako argumenty. Modely su po spajani <b>modifikovane</b>
     * - invalidovane.
     * @param model1 prvy model
     * @param model2 druhy model
     * @param priorityFirst priznak, ci ma prioritu prvy model
     */
    public ModelCombiner(Information model1, Information model2, boolean priorityFirst) {
        // Pracuje obdobne ako prvy konstruktor
        if(model1!=null && model2!=null){
            if(!model1.getMMConfiguration().equals(model2.getMMConfiguration())){
                throw new ParsingException("ModelMerger<Constructor>::\n\t"+
                        "ERROR: Arguments \"model1\" and \"model2\" must be equivalent," +
                        " they have to share same MetaModelConfiguration.");
            }
            if(PerChildHandler.isPerChildInBranch(model1.getMMConfiguration())){
                PerChildHandler.contractInformationPerChild(model1);
            }
            if(PerChildHandler.isPerChildInBranch(model1.getMMConfiguration())){
                PerChildHandler.contractInformationPerChild(model1);
            }
            // Podla priority volim spajanie
            if(priorityFirst){
                model = model1;
                combineInformation(model1, model2, model);
            } else {
                model = model2;
                combineInformation(model2, model1, model);
            }
            PerChildHandler.expandInformationPerChild(model);
        } else if(model1 == null && model2 != null) {
            model = model2;
        } else if(model1 != null && model2 == null){
            model = model1;
        } else {
            model = null;
        }
    }

    /**
     * Vrati vysledny model.
     * @return
     */
    public Information getModel(){
        return model;
    }

    /**
     * Spoji vysledny model s dalsim modelom. higherPriority urcuje, ci ma
     * dalsi model vyssiu prioritu.
     * @param nextModel dalsi model na spajanie
     * @param higherPriority ak je nastaveny, vyssiu prioritu ma nextModel
     */
    public void combineNext(Information nextModel, boolean higherPriority) {
        if(nextModel==null){
            return;
        }
        if(model==null){
            model = nextModel;
            return;
        }
        Information merged = model;
        if(higherPriority){
            model = nextModel;
            combineInformation(nextModel, merged, model);
        } else {
            combineInformation(merged, nextModel, model);
        }
    }

    /**
     * Metoda kombinuje dva modely konfiguracie. Pritom sa znehodnotia oba dodane modely.
     * @param first
     * @param second
     * @param merged
     */
    private void combineInformation(Information first, Information second, Information merged){
        List<Information> firstChildren;
        List<Information> secondChildren;
        // Nova tabulka pre spojeny model
        Map<ConfigurationType, List<Information>> mergedChildren = new HashMap<ConfigurationType, List<Information>>();
        // Pre kazdy mozny konfiguracny typ
        for(ConfigurationType configuration : first.getMMConfiguration().getChildrenToProcess()){
            // Nastavenie odkazov na zoznamy, menej volani v priebehu iteracie
            firstChildren = first.getChildren().get(configuration);
            secondChildren = second.getChildren().get(configuration);
            if(!first.getChildren().containsKey(configuration) && !second.getChildren().containsKey(configuration)){
                // Ak informacie tohto typu nema ani jeden zo spajanych modelov, tak sa nic neriesi
            } else if(!first.getChildren().containsKey(configuration)){
                // Ak jedna z vetiev nie je, testujem ci sa ma spajat, lebo
                // inak sa nemaju pridat potomkovia z druheho modelu (prvy model
                // pretlaci svoje - v tomto pripade pretlaci nic)
                if(configuration.isMergingPoint()) {
                    mergedChildren.put(configuration, secondChildren);
                    for(Information child : mergedChildren.get(configuration)){
                        child.setParent(merged);
                    }
                }
            } else if(!second.getChildren().containsKey(configuration)){
                // Ak ekvivalent neexistuje tak nie je co riesit
                mergedChildren.put(configuration, firstChildren);
                for(Information child : mergedChildren.get(configuration)){
                    child.setParent(merged);
                }
            } else {
                // Tu je pripad, ze existuju potomkovia tohto typu v oboch modeloch
                if(configuration.isMergingPointInBranch()) {
                    // Ak sa ma riesit kombinovanie
                    for(Information child : firstChildren){
                        // Pre kazdeho potomka uvedeneho typu z prioritnejsieho modelu
                        // hladam ekvivalent v druhom modeli
                        Information equivalent = findEquivalent(child, secondChildren);
                        if(equivalent!=null){
                            // Ak mam ekvivalent, tak spajam co je pod nim (rekurzia)
                            combineInformation(child, equivalent, child);
                            // A ekvivalent vynecham z druheho modelu
                            secondChildren.remove(equivalent);
                        }
                    }
                    if(configuration.isMergingPoint()){
                        // Ak sa spaja aj prave na tejto urovni, tak pridam tie,
                        // ktore ostali v druhom modeli
                        firstChildren.addAll(secondChildren);
                    }
                }
                // Vysledny zoznam potomkov sa ulozi do skombinovaneho modelu
                // Ak sa nekombinovalo, je to iba zoznam z prioritneho modelu
                mergedChildren.put(configuration, firstChildren);
                // Nastavime rodicov
                for(Information child : mergedChildren.get(configuration)){
                    child.setParent(merged);
                }
            }
        }
        merged.setChildren(mergedChildren);
    }

    /**
     * Aproximacna metoda na najdenie ekvivalentu k informacii. Jej kvalita
     * zavisi od poskytnutej implementacie InformationComparator.
     * @param information
     * @param informations
     * @return
     */
    private Information findEquivalent(Information information, List<Information> informations){
        for(Information candidate : informations){
            // Najde prvy vyskyt, ktory sa "podoba" argumentu information
            if(information.getMMConfiguration().getInformationComparator().compareInformations(information, candidate)){
                return candidate;
            }
        }
        return null;
    }
}
