package BTE.configuration.model.metamodel.extractors;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.metamodel.interfaces.InformationExtractor;
import BTE.configuration.model.model.interfaces.Information;
import java.util.Set;

/**
 * Predvoleny zakladny extraktor informacii pre vyskyty anotacii.
 * @author Milan
 */
public class DefaultAnnotationInformationExtractor implements InformationExtractor {

    public Object[] getValues(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        // A primitivny navrat hodnot pre generovanie
        Set<AnnotationTypeInstance> annotations = (Set<AnnotationTypeInstance>)value;
        return annotations.toArray(new AnnotationTypeInstance[]{});
    }

    public boolean generate(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        // Primitivny test, ci je co generovat
        if(value!=null && (value instanceof Set)){
            Set<AnnotationTypeInstance> annotations = (Set<AnnotationTypeInstance>)value;
            if(annotations.isEmpty())
                return false;
            return true;
        }
        return false;
    }

}
