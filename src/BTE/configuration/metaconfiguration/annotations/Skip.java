package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Oznacenie informacie, ktora sa nema mapovat do XML.
 * @author Milan
 */
@MapsTo(name="skip")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface Skip {
    /**
     * Hlbka vynechania. Ak pod danou konf. informaciou nasleduju dalsie,
     * tak tato hodnota mi povie ako hlboko v podstrome mam preskakovat
     * informacie.
     * @return
     */
    int depth() default Integer.MAX_VALUE;
}
