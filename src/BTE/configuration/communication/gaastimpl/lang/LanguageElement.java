package BTE.configuration.communication.gaastimpl.lang;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;

/**
 * Abstraktna trieda s metodami, ktore sa daju zovseobecnit pre kazdy
 * jazykovy element.
 * @author Milan
 */
public abstract class LanguageElement {
    private String name;
    private String canonicalName;
    private Set<AnnotationMirror> annotations = new HashSet<AnnotationMirror>();
    
    public AnnotationMirror getAnnotation(String canonicalName) {
        for(AnnotationMirror annotation : annotations) {
            if(annotation.getAnnotationType().toString().equals(canonicalName)) {
                return annotation;
            }
        }
        return null;
    }

    public Set<AnnotationMirror> getAnnotations() {
        return annotations;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getName() {
        return name;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Equals">
    @Override
    public boolean equals(Object object) {
        if(this.getClass().equals(object.getClass())) {
            if(this.getCanonicalName()!=null) {
                return this.getCanonicalName().equals(((LanguageElement)object).getCanonicalName());
            }
            return false;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return (this.getCanonicalName() != null ? this.getCanonicalName().hashCode() : 0);
    }
    // </editor-fold>
}
