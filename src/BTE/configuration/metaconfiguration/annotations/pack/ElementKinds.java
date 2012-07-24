package BTE.configuration.metaconfiguration.annotations.pack;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Metainformacia so zoznamom generickych mien (ich mapovanim). Jej vlasnosti
 * su nazvane podla podporovanych jazykovych elementov, podla nazvov je lahko
 * odvoditelny vyznam danej vlastnosti.
 * @author Milan
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementKinds {
    String _package() default "package";
    String _enum() default "enum";
    String _class() default "class";
    String _annotationType() default "annotation";
    String _interface() default "interface";
    String _field() default "field";
    String _parameter() default "parameter";
    String _method() default "method";
    String _constructor() default "constructor";
}
