package BTE.configuration.parsing.model.combining;

import BTE.configuration.communication.Priority;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.model.model.interfaces.Information;

/**
 * Trieda automatizujuca spajanie viacerych modelov.
 * @author Milan
 */
public class MultiModelCombiner {
    /**
     * Spaja viacero modelov. Pritom znehodnoti dodane modely.
     * Priorita klesa z lava do prava.
     * @param annotations
     * @param models
     * @return
     */
    public static Information combineModels(MetaConfigurationLoader loader, Information annotations, Information... models){
        if(models.length>1){
            ModelCombiner mm = new ModelCombiner(models[0], models[1], true);
            // Ak je co spajat tak najprv pospajam modely z XML
            for(int i = 2; i < models.length; i ++){
                mm.combineNext(models[i], false);
            }
            mm.combineNext(annotations, loader.getPriority()==Priority.ANNOTATIONS);
            return mm.getModel();
        } else if(models.length==1){
            return (new ModelCombiner(annotations, models[0], loader.getPriority())).getModel();
        } else {
            return annotations;
        }
    }

    /**
     * Spaja viacero modelov. Pritom znehodnoti dodane modely.
     * Priorita klesa z lava do prava.
     * @param annotations
     * @param models
     * @return
     */
    public static Information combineModels(Information... models){
        if(models.length>1){
            ModelCombiner mm = new ModelCombiner(models[0], models[1], true);
            for(int i = 2; i < models.length; i ++){
                mm.combineNext(models[i], false);
            }
            return mm.getModel();
        } else if(models.length==1){
            return models[0];
        } else {
            return null;
        }
    }
}
