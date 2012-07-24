package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metaanotacia na pridanie wrappera, elementu ktory nema mapovanie na anotacie.
 * Je nim mozne napr. zgrupovat elementy podla cieloveho elementu zdr. kodu.
 * @author Milan
 */
@MapsTo(name="wrapper")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface Wrapper {
    enum TargetCoupling {
        PACKAGE, TOP_PACKAGE, CLASS, TOP_CLASS, TARGET, TOP_TARGET, ONE, PER_CHILD
    }

    /**
     * Definicia politiky zgrupovania elementom pod wrapperom. Napr. pri CLASS
     * sa zgrupuju podla triedy, pricom sa snazi hladat spolocnu triedu. Pri
     * TOP_CLASS vybera pre kazdy element triedu najhlbsie v hierarchii kodov (v zmysle vnutornych tried).
     * PER_CHILD generuje pre kazdeho potomka jeden element. ONE generuje jediny
     * wrapper.
     * @return
     */
    TargetCoupling targetCoupling() default TargetCoupling.PACKAGE;

    /**
     * Ak je hodnota true, wrapper sa vklada medzi anotovany an. typ (resp.
     * vlastnost) a jeho potomkov. Inak sa vklada medzi potomkov rodica.
     * @return
     */
    boolean childWrapping() default true;

    /**
     * Nazvy elementov, ktore maju byt spolu zabalene, ak nie je uvedene,
     * zabalia sa vsetky na danej urovni.
     * @return
     */
    String[] wrappedElements();

    /**
     * Nazov generovaneho elementu.
     * @return
     */
    String wrapperName();

    /**
     * Nazov typu generovaneho elementu v XSD.
     * @return
     */
    String typeName() default "${name}-type";
}
