package BTE.configuration.communication.interfaces;

import java.util.Set;

/**
 * Rozhranie anotacneho scannera, pre potreby ziskania informacii o vyskytoch
 * anotacii.
 * @author Milan
 */
public interface AnnotationScanner {
    /**
     * Ma vratit mnozinu vyskytov anotacii daneho anotacneho typu. Ten je urceny
     * parametrom metody. Mala by stacit tato jedina metoda.
     * @param qualifiedName
     * @return
     */
    public Set<AnnotationTypeInstance> getAnnotationsOfType(String qualifiedName);
}
