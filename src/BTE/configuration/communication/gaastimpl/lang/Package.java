package BTE.configuration.communication.gaastimpl.lang;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;

/**
 * Trieda zgrupujuca objekty predstavujuce balik v Jave. Sluzi na modelovanie
 * jazyka pre GAAST potreby.
 * @author Milan
 */
public class Package extends LanguageElement {
    
    private Set<Class> declaredClasses = new HashSet<Class>();

    public Package(String name) {
        super.setName(name);
        super.setCanonicalName(name);
    }

    public Set<Class> getDeclaredClasses() {
        return declaredClasses;
    }
    
    @Override
    public void setName(String name) {
        super.setName(name);
        super.setCanonicalName(name);
    }
    
    @Override
    public void setCanonicalName(String canonicalName) {
        super.setCanonicalName(canonicalName);
        super.setName(canonicalName);
    }
}
