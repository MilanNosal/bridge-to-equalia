package BTE.configuration.communication.aptscanner;

import BTE.configuration.communication.interfaces.AnnotationScanner;
import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Milan
 */
public class APTScanner implements AnnotationScanner {
    
    RoundEnvironment roundEnv;
    Set<? extends TypeElement> annotations;
    
    public APTScanner(RoundEnvironment roundEnv, Set<? extends TypeElement> annotations) {
        this.roundEnv = roundEnv;
        this.annotations = annotations;
    }

    @Override
    public Set<AnnotationTypeInstance> getAnnotationsOfType(String qualifiedName) {
        Set<AnnotationTypeInstance> retAnnotations = new HashSet<AnnotationTypeInstance>();
        TypeElement ann = null;
        for(TypeElement te : annotations) {
            if(te.getQualifiedName().toString().equals(qualifiedName)) {
                ann = te;
                break;
            }
        }
        if(ann==null) {
            return retAnnotations;
        }
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(ann);
        return retAnnotations;
    }
    
}
