package BTE.configuration.model.metamodel.extractors;

import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.metamodel.interfaces.InformationExtractor;
import BTE.configuration.model.model.interfaces.Information;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

/**
 * Predvoleny zakladny extraktor informacii pre vlastnosti anotacii. Trosku
 * komplikovanejsi postup je potrebny, musim pamatat na rozne typy vlastnosti,
 * ktore som mohol dostat.
 * @author Milan
 */
public class DefaultPropertyInformationExtractor implements InformationExtractor {

    public Object[] getValues(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        switch(configuration.getMappingOfConfigurationToSources().getSourceType()){
            case DECL_PROP_ARRAY_ANNOTATION:
            case DECL_PROP_ARRAY_ENUM:
            case DECL_PROP_ARRAY_PRIMITIVE:
            case DECL_PROP_ARRAY_STRING:
            {
                if(value==null){
                    return new Object[]{};
                }
                if(!(value.getClass().isArray())){
                    throw new ParsingException("BTE.DefaultPropertyInformationExtractor:: ERROR:\n\tArray expected!");
                }
                List<Object> list = new LinkedList<Object>();
                for(int i = 0; i < Array.getLength(value); i++){
                    // A pre kazdu polozku pola generujem informaciu
                    // (zdrojovy objekt je informacie je komponent pola)
                    list.add(Array.get(value, i));
                }
                return list.toArray();
            }
            case DECL_PROP_ANNOTATION:
            case DECL_PROP_ENUM:
            case DECL_PROP_PRIMITIVE:
            case DECL_PROP_STRING:
            {
                return new Object[]{value};
            }
            default: {
                throw new ParsingException("BTE.DefaultPropertyInformationExtractor:: ERROR:\n\tI was not supposed to get here!");
            }
        }
    }

    public boolean generate(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        return true;
    }

}
