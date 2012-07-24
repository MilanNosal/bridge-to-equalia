package BTE.configuration.model.metamodel.extractors;

import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.metamodel.interfaces.InformationExtractor;
import BTE.configuration.model.model.interfaces.Information;
import java.lang.reflect.Modifier;
import javax.lang.model.element.ElementKind;

/**
 * Ukazka implementacie InformationExtractor, tato implementacia generuje
 * informaciu s hodnotou true do modelu konfiguracie v pripade, ze rodic
 * (parent) je v kontexte cieloveho jazykoveho elementu, ktory ma modifikator
 * static.
 * @author Milan
 */
public class StaticModifierExtractor implements InformationExtractor {

    public Object[] getValues(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        // Kedze testy uz prebehli v generate, a tato metoda je volana iba ak
        // generate vratila true, nemusim znova robit testy (hodnota je vzdy true)
        return new String[]{"true"};
    }

    public boolean generate(Information anchor, Information parent, ConfigurationType configuration, Object value) {
        if(parent==null || parent.getInformationSource()==null){
            return false;
        }
        ElementKind kind = parent.getInformationSource().getJavaElementKind();
        // Test na typ, budem podporovat iba field a method
        switch(kind){
            case FIELD:{
                int modifiers = parent.getInformationSource().getSourceField().getModifiers();
                if(Modifier.isStatic(modifiers)){
                    return true;
                }
                break;
            }
            case METHOD:{
                int modifiers = parent.getInformationSource().getSourceMethod().getModifiers();
                if(Modifier.isStatic(modifiers)){
                    return true;
                }
                break;
            }
        }
        return false;
    }

}
