package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Testovacia metaanotacie pre vlastny extractor informacii. Vie pridavat
 * informaciu o modifikatore static nad cielovym jazykovym elementom. Ak je
 * modifikator pouzity, tak prida element/atribut s hodnotou true, inak nespravi
 * nic.
 * @author Milan
 */
@MapsTo(name="staticModifier")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface StaticModifier {
    /**
     * Priznak, ci sa informacia ma mapovat na element alebo atribut.
     * @return
     */
    boolean element() default true;
}
