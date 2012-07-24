package BTE.configuration.communication.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.lang.model.element.ElementKind;

/**
 * Wrapper, ktory zabaluje potrebne informacie o anotacii.
 * @author Milan
 */
public interface AnnotationTypeInstance {
    /**
     * Metoda pre vratenie package, ak je ten zdrojom anotacie.
     * @return
     */
    Package getSourcePackage();

    /**
     * Vrati Class objekt, nad ktorym sa nachadza anotacia, ak je ElementKind
     * CLASS, INTERFACE, ENUM alebo ANNOTATION_TYPE
     * @return
     */
    Class getSourceClass();

    /**
     * Vrati objekt metody, nad ktorou sa anotacia nachadza, ak je elementKind
     * METHOD
     * @return
     */
    Method getSourceMethod();

    /**
     * Objekt konstruktora, ktory ma anotaciu, ak je elementKind CONSTRUCTOR.
     * @return
     */
    Constructor getSourceConstructor();

    /**
     * Objekt clenskej premennej.
     * @return
     */
    Field getSourceField();

    /**
     * Vrati cislo vyjadrujuce poradie parametra, ak ide o
     * ELementKind.PARAMETER.
     * Vtedy musi byt not null bud method alebo constructor.
     */
    int getParameterNumber();

    /**
     * Navracia druh Java elementu, nad ktorym je uvedena anotacia.
     * Ovsem sa treba predpokladat, ze sa tu vyskytnu iba tie typy, ktore
     * umoznuju pouzitie anotacie. Tie vychadzaju z enumeracneho typu
     * ElementType. Pouzity je vsak tento, pretoze tento rozlisuje tie typy,
     * ktore ElementType uvadza pod spolocnou konstantou ElementType.TYPE.
     * Pr.: z String A2XandM.tool.communication.interfaces.AnnotationInstance.getJavaElementName()
     *      je to ElementKind.METHOD
     * @return
     */
    ElementKind getJavaElementKind();

    /**
     * A napokon samotna anotacia. Uvazoval som tiez o nejakej vlastnej
     * zaobalujucej triede, ak by sm to chcel robit implementacne nezavisle
     * (APT pokial viem neposkytuje priamo anotacie, ale napr. AnnotationMirror),
     * zatial vsak pre jednoduchost ponechavam rozhranie Annotation.
     * @return
     */
    Annotation getAnnotation();

    /**
     * Toto je hlavne pre moje potreby. Ulahci mi spracovanie.
     * Pre spravne fungovanie je potrebne pouzivat na generovanie nazvu z
     * metody/konstruktora metodu BTE.tool.model.utilities.Utilities.getMethodsCanonicalName.
     * V pripade parametra sa vracia nazov metody zretazeny s poradovym cislom
     * parametra oddelenym od metody bodkou.
     * @return
     */
    String getJavaElementCanonicalName();

}
