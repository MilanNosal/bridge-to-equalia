package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tato metainformacia by mala vytvorit kopiu vetvy definovanej konf. typom,
 * na ktorom je pouzita a priradit ho novemu rodicovi definovanom vlastnostou
 * copysParent.
 * @author Milan
 */
@MapsTo(name="copyBranch")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface CopyBranch {
    /**
     * Novy rodic kopie.
     * @return
     */
    String copysParent();
}
