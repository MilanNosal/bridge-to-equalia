package BTE.configuration.communication.scannotationscanner;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;

/**
 * Trieda mi poskytne metody pre prehladavanie triedy za ucelom
 * identifikovania anotacii nad danou triedou.
 * @author Milan
 */
class ClassAnnotationScanner {
    /**
     * Trieda, ktora bude skenovana.
     */
    private Class clazz;

    /**
     * Konstruktor s triedou urcenou na skenovanie.
     * @param clazz
     */
    protected ClassAnnotationScanner(Class clazz){
        this.clazz = clazz;
    }

    /**
     * Metoda skenuje celu triedu na vyskyt anotacii. Ako parameter berie
     * hesovaciu tabulku, kde su k nazvom anotacii priradene mnoziny
     * ich vyskytov. Tuto tabulku v tele upravuje a potom je vracia.
     * Upravuje tabulku, ktoru dostane ako argument, a teda nie je potrebne
     * pracovat s navratovou hodnotou (pretoze sa nevytvara nova tabulka).
     * Vdaka charakteru, akym funguje scannotation, nepotrebujem skenovat
     * vnutorne triedy(tie dostanem ako triedy s vyskytom anotacii, ak to
     * budem potrebovat).
     * @param annotations
     * @return
     */
    protected Map<String, Set<AnnotationTypeInstance>> scanClass(Map<String, Set<AnnotationTypeInstance>> annotations){
        // Otestujem na anonymne a lokalne triedy, tie nema zmysel spracovavat
        if(clazz.isAnonymousClass() || clazz.isLocalClass()){
            return annotations;
        }

        ElementKind javaElementKind = resolveElementKindOfClass();
        // Prv ziskam anotacie nad samotnou triedou
        for(Annotation annotation : clazz.getAnnotations()){
            // Nazov anotacneho typu je hesovaci kluc do tabulky
            String annotationName = annotation.annotationType().getName();
            Set<AnnotationTypeInstance> set;
            if(annotations.containsKey(annotationName)){
                set = annotations.get(annotationName);
                // a este test, ci trieda nie je pre package
                if(javaElementKind.equals(ElementKind.PACKAGE)){
                    set.add(new AnnotationTypeInstanceImpl(clazz.getPackage(), ElementKind.PACKAGE, annotation));
                } else {
                    set.add(new AnnotationTypeInstanceImpl(clazz, javaElementKind, annotation));
                }
            } else {
                set = new HashSet<AnnotationTypeInstance>();
                if(javaElementKind.equals(ElementKind.PACKAGE)){
                    set.add(new AnnotationTypeInstanceImpl(clazz.getPackage(), ElementKind.PACKAGE, annotation));
                } else {
                    set.add(new AnnotationTypeInstanceImpl(clazz, javaElementKind, annotation));
                }
                annotations.put(annotationName, set);
            }
        }
        // Ak ide o package, nema zmysel riesit metody a clenske premenne
        if(javaElementKind.equals(ElementKind.PACKAGE)){
            return annotations;
        }
        // Nasledne nad jej metodami ( a parametrami metod )
        for(Method method : clazz.getDeclaredMethods()){
            getAnnotationsUponMethod(method, annotations);
        }
        // Este clenske premenne
        for(Field field : clazz.getDeclaredFields()){
            getAnnotationsUponField(field, annotations);
        }
        // Konstruktory
        for(Constructor constructor : clazz.getDeclaredConstructors()){
            getAnnotationsUponConstructor(constructor, annotations);
        }

        return annotations;
    }

    /**
     * Privatna metoda, ktora skenuje clensku premennu na vyskyt anotacie.
     * Funguje podobne ako metoda skenujuca triedu.
     * @param field
     * @param annotations
     * @return
     */
    private Map<String, Set<AnnotationTypeInstance>> getAnnotationsUponField
            (Field field, Map<String, Set<AnnotationTypeInstance>> annotations){
        ElementKind javaElementKind = ElementKind.FIELD;
        // Ziskame anotacie nad clenskou premennou
        for(Annotation annotation : field.getAnnotations()){
            String annotationName = annotation.annotationType().getName();
            Set<AnnotationTypeInstance> set;
            if(annotations.containsKey(annotationName)){
                set = annotations.get(annotationName);
                set.add(new AnnotationTypeInstanceImpl(field, javaElementKind, annotation));
            } else {
                set = new HashSet<AnnotationTypeInstance>();
                set.add(new AnnotationTypeInstanceImpl(field, javaElementKind, annotation));
                annotations.put(annotationName, set);
            }
        }
        return annotations;
    }

    /**
     * Metoda, ktora skenuje metodu na vyskyt anotacii. Samozrejme sleduje aj
     * parametre danej metody.
     * @param method
     * @param annotations
     * @return
     */
    private Map<String, Set<AnnotationTypeInstance>> getAnnotationsUponMethod
            (Method method, Map<String, Set<AnnotationTypeInstance>> annotations){
        ElementKind javaElementKind = ElementKind.METHOD;
        // Prv ziskam anotacie nad samotnou metodou
        for(Annotation annotation : method.getAnnotations()){
            String annotationName = annotation.annotationType().getName();
            Set<AnnotationTypeInstance> set;
            if(annotations.containsKey(annotationName)){
                set = annotations.get(annotationName);
                set.add(new AnnotationTypeInstanceImpl(method, javaElementKind, annotation));
            } else {
                set = new HashSet<AnnotationTypeInstance>();
                set.add(new AnnotationTypeInstanceImpl(method, javaElementKind, annotation));
                annotations.put(annotationName, set);
            }
        }
        // A teraz pozriem parametre
        javaElementKind = ElementKind.PARAMETER;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for(int parameterNumber = 0; parameterNumber < parameterAnnotations.length; parameterNumber++){
            
            for(Annotation annotation : parameterAnnotations[parameterNumber]){
                String annotationName = annotation.annotationType().getName();
                Set<AnnotationTypeInstance> set;
                if(annotations.containsKey(annotationName)){
                    set = annotations.get(annotationName);
                    set.add(new AnnotationTypeInstanceImpl(method, parameterNumber, javaElementKind, annotation));
                } else {
                    set = new HashSet<AnnotationTypeInstance>();
                    set.add(new AnnotationTypeInstanceImpl(method, parameterNumber, javaElementKind, annotation));
                    annotations.put(annotationName, set);
                }
            }
        }
        return annotations;
    }
    
    /**
     * Metoda skuma konstruktor, funguje obdobne ako metoda skenujuca metody.
     * @param constructor
     * @param annotations
     * @return
     */
    private Map<String, Set<AnnotationTypeInstance>> getAnnotationsUponConstructor
            (Constructor constructor, Map<String, Set<AnnotationTypeInstance>> annotations){
        ElementKind javaElementKind = ElementKind.CONSTRUCTOR;
        // Anotacie priamo nad konstruktorom
        for(Annotation annotation : constructor.getAnnotations()){
            String annotationName = annotation.annotationType().getName();
            Set<AnnotationTypeInstance> set;
            if(annotations.containsKey(annotationName)){
                set = annotations.get(annotationName);
                set.add(new AnnotationTypeInstanceImpl(constructor, javaElementKind, annotation));
            } else {
                set = new HashSet<AnnotationTypeInstance>();
                set.add(new AnnotationTypeInstanceImpl(constructor, javaElementKind, annotation));
                annotations.put(annotationName, set);
            }
        }
        // A teraz pozriem parametre konstruktora
        javaElementKind = ElementKind.PARAMETER;
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        for(int parameter = 0; parameter < parameterAnnotations.length; parameter++){
            for(Annotation annotation : parameterAnnotations[parameter]){
                String annotationName = annotation.annotationType().getName();
                Set<AnnotationTypeInstance> set;
                if(annotations.containsKey(annotationName)){
                    set = annotations.get(annotationName);
                    set.add(new AnnotationTypeInstanceImpl(constructor, parameter, javaElementKind, annotation));
                } else {
                    set = new HashSet<AnnotationTypeInstance>();
                    set.add(new AnnotationTypeInstanceImpl(constructor, parameter, javaElementKind, annotation));
                    annotations.put(annotationName, set);
                }
            }
        }
        // Malo by byt hotovo
        return annotations;
    }

    /**
     * Ma urcit ElementKind triedy.
     * @return
     */
    private ElementKind resolveElementKindOfClass(){
        if("package-info".equals(clazz.getSimpleName())){
            return ElementKind.PACKAGE;
        }
        if(clazz.isAnnotation()){
            return ElementKind.ANNOTATION_TYPE;
        }
        if(clazz.isEnum()){
            return ElementKind.ENUM;
        }
        if(clazz.isInterface()){
            return ElementKind.INTERFACE;
        }
        return ElementKind.CLASS;
    }

    /**
     * Metoda ma skenovat triedu za vyskytom anotacii konkretneho typu.
     * Pouzije metodu skenujucu za vsetkymi typmi a vyberie z hesovacej
     * tabulky iba anotacie, ktore su daneho typu.
     * @param annotationType
     * @param annotations
     * @return
     */
    protected Set<AnnotationTypeInstance> scanClass(String annotationType, Set<AnnotationTypeInstance> annotations){
        Set<AnnotationTypeInstance> found = scanClass(new HashMap<String, Set<AnnotationTypeInstance>>()).get(annotationType);
        if(found==null){
            found = new HashSet<AnnotationTypeInstance>();
        }
        annotations.addAll(found);
        return annotations;
    }

    /**
     * "Optimalizacna" metoda, funguje obdobne ako metoda na skenovanie triedy
     * za ucelom najdenia vsetkych vyskytov anotacii, avsak berie aj zoznam
     * pre nas podstatnych anotacnych typov, a pridava do tabulky len tie
     * vyskyty, ktore nas zaujimaju. Aby som usetril na pisani, pouzivam opat tu
     * istu metodu na prehladavanie, a po prehladani vymazem vyskyty, ktore su
     * naviac.
     * POZOR! kedze sa upravuje ta ista tabulka, ktora sa predava ako argument
     * metody, je treba si uvedomit, ze ak argument obsahoval aj vyskyty
     * pre nas nezaujimavych anotacii, po volani tejto metody uz NEBUDE.
     * @param interestingTypes
     * @param annotations
     * @return
     */
    protected Map<String, Set<AnnotationTypeInstance>> scanClass
            (Set<String> interestingTypes, Map<String, Set<AnnotationTypeInstance>> annotations){
        scanClass(annotations);
        Set<String> temp = new HashSet<String>();
        for(String annotationType : annotations.keySet()){
            if(!interestingTypes.contains(annotationType)){
                temp.add(annotationType);
            }
        }
        for(String type : temp)
            annotations.remove(type);
        return annotations;
    }
}
