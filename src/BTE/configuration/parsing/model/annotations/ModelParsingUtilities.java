package BTE.configuration.parsing.model.annotations;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;

/**
 * Trieda s pomocnymi metodami urcenymi pre spracovanie konfiguracie v
 * zdrojovych kodoch.
 * @author Milan
 */
public class ModelParsingUtilities {
    /**
     * Metoda sa pokusi na zaklade vstupnych parametrov urcit, aku hodnotu
     * ma mat vypis cieloveho elementu pre konfiguracny typ child s rodicom
     * parent. Pritom dava moznost vyberu urcenia cieloveho elementu prostrednictvom
     * anotacie (annotation) alebo priamo retazcom reprezentujucim cielovy
     * element (targetQualifiedName). Ak nie je uvedena ani jedna moznost,
     * berie do uvahy kontext.
     * @param parent rodic informacie, pre ktoru urcujem target
     * @param child konfiguracia informacie, pre ktoru urcujem target
     * @param annotation zdroj informacie, pre ktoru urcujem target
     * @param targetQualifiedName cele meno cieloveho elementu
     * @return
     */
    public static String buildTargetElementValue(Information parent, ConfigurationType child, AnnotationTypeInstance annotation, String targetQualifiedName){
        if(child.getMappingOfTargetElement().getQNameOfTargetProcView()==XMLProcessing.SKIP_PROCESS){
            // Ak sa nemapuje informacia o cielovom elemente, vraciam prazdny retazec
            return "";
        }
        switch(child.getMappingOfTargetElement().getQNameOfTargetProcType()){
            case FULL_PRINT:{
                // Pripad vypisu celeho nazvu
                if(annotation == null && targetQualifiedName == null){
                    // Ak nie je poskytnuta ani anotacia ani retazcova reprezentacia
                    // vraciam kontext
                    return parent.getTargetQualifiedName();
                } else if(annotation==null) {
                    // Ak nie je anotacia, vraciam retazcovu reprezentaciu
                    return targetQualifiedName;
                }
                // Inak beriem cielovy element
                return annotation.getJavaElementCanonicalName();
            }
            case SIMPLE_PRINT:{
                // Jednoduchy vypis
                String retVal = null;
                // Do retVal najdem kontext informacie
                if(annotation==null && targetQualifiedName==null){
                    retVal = parent.getTargetQualifiedName();
                } else if(annotation==null){
                    retVal = targetQualifiedName;
                } else {
                    retVal = annotation.getJavaElementCanonicalName();
                }
                
                if(retVal.equals("")){
                    // v pripade prazdneho retazca nemam vratit co ine
                    return "";
                }
                
                if(retVal.endsWith(")")){
                    // A little tricky (pri metodach a konstruktoroch):
                    // package.Class.method(java.lang.String)
                    // nemozem pouzit lastIndexOf(".")
                    String temp = targetQualifiedName.substring(0, targetQualifiedName.lastIndexOf("("));
                    return retVal.substring(temp.lastIndexOf("."));
                }
                return getSimpleName(retVal);
            }
            case CONTEXT_PRINT:{
                // Najdem ozajstny kontext aktualnej informacie(nie parent)
                String retVal;
                if(annotation==null && targetQualifiedName==null){
                    retVal = parent.getTargetQualifiedName();
                } else if(annotation==null){
                    retVal = targetQualifiedName;
                } else {
                    retVal = annotation.getJavaElementCanonicalName();
                }
                // Ak je prazdny, nie je co riesit
                if(retVal.equals("")){
                    return retVal;
                }
                // Proste premazem to, co je naviac - prefix, ktory je uz definovany
                // kontextom
                String replacer = findJavaElementContext(parent);
                // Ak kontext rodica je prazdny, vraciam cely nazov
                if(replacer.equals("")){
                    return retVal;
                }
                // Ak su rovnake, kontext sa nemeni
                if(retVal.equals(replacer)){
                    return "";
                }
                // Ak kontext definuje aktualny cielovy element, odrezem nadbytocnu
                // cast
                if(retVal.startsWith(replacer)){
                    retVal = retVal.substring(replacer.length()+1);
                }
                return retVal;
            }
            default:{
                throw new ParsingException("ModelParsingUtilities.buildTargetElementValue()::" +
                        "\n\tI was not supposed to get here!");
            }
        }
    }

    /**
     * Metoda vracia jednoduche meno elementu, hlada posledny prvok oddeleny
     * '.' alebo '$'.
     * @param string
     * @return
     */
    private static String getSimpleName(String string){
        if(string.contains(".") && string.lastIndexOf("$")<string.lastIndexOf(".")){
            // Osetruje pripad, ked je posledny prvok oddeleny '.'
            return string.substring(string.lastIndexOf(".")+1);
        } else if (string.contains("$")) {
            // Osetruje pripad, ak je posledny prvok oddeleny '$'
            return string.substring(string.lastIndexOf("$")+1);
        } else {
            // Inak je k dispozicii len jednoduche meno
            return string;
        }
    }

    /**
     * Metoda na najdenie anotacie (pozor, moze ist aj o tzv. dummy anotaciu,
     * t.j. objekt annotationInstance, ktory nema odkaz na anotaciu, iba
     * informacie o cielovom elemente) kontextu, do ktoreho idem pridavat informacie.
     * Tu hlada ozajstny kontext, t.j. kontext nad ktorym je dana informacia.
     * @param information
     * @return
     */
    public static AnnotationTypeInstance findJavaElementActualContext(Information information){
        while(information!=null){
            // Sleduje cestu az ku korenovej informacii
            if(information.getInformationSource()!=null){
                return information.getInformationSource();
            }
            information = information.getParent();
        }
        return null;
    }

    /**
     * Pomocna metoda na urcenie typu triedy. Moze vracat jednu z hodnot
     * ANNOTATION_TYPE, ENUM, INTERFACE, CLASS. Alebo, ak je parameter null,
     * vracia null.
     * @param clazz
     * @return
     */
    public static ElementKind determineClassType(Class clazz){
        if(clazz==null){
            return null;
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
     * Metoda na najdenie triedy, ku ktorej sa informacia vztahuje. Ak napr.
     * cielovy element informacie je metoda, tak tato metoda ma vratit triedu,
     * ktora definuje danu metodu. Ak informacia nema definovany kontext, alebo
     * kontext je balik, vracia null.
     * @param information
     * @return
     */
    public static Class findTargetClass(Information information){
        AnnotationTypeInstance context = findJavaElementActualContext(information);
        if(context!=null){
            switch(context.getJavaElementKind()){
                case ANNOTATION_TYPE:
                case CLASS:
                case INTERFACE:
                case ENUM:{
                    return context.getSourceClass();
                }
                case CONSTRUCTOR:{
                    return context.getSourceConstructor().getDeclaringClass();
                }
                case METHOD:{
                    return context.getSourceMethod().getDeclaringClass();
                }
                case FIELD:{
                    return context.getSourceField().getDeclaringClass();
                }
                case PARAMETER:{
                    return context.getSourceMethod()==null?
                        context.getSourceConstructor().getDeclaringClass():
                        context.getSourceMethod().getDeclaringClass();
                }
                case PACKAGE:{
                    return null;
                }
                default:{
                    throw new ParsingException("ModelParsingUtilities<findEnclosingClass()>::\n\t" +
                            "ERROR: I was not supposed to get here!");
                }
            }
        }
        return null;
    }

    /**
     * Metoda na najdenie baliku, ku ktoremu sa dana informacia vztahuje. Opat
     * ak nie je mozne najst kontext, vracia null.
     * @param information
     * @return
     */
    public static Package findTargetPackage(Information information){
        AnnotationTypeInstance context = findJavaElementActualContext(information);
        if(context!=null){
            switch(context.getJavaElementKind()){
                case ANNOTATION_TYPE:
                case CLASS:
                case INTERFACE:
                case ENUM:{
                    return context.getSourceClass().getPackage();
                }
                case PACKAGE:{
                    return context.getSourcePackage();
                }
                case CONSTRUCTOR:{
                    return context.getSourceConstructor().getDeclaringClass().getPackage();
                }
                case METHOD:{
                    return context.getSourceMethod().getDeclaringClass().getPackage();
                }
                case FIELD:{
                    return context.getSourceField().getDeclaringClass().getPackage();
                }
                case PARAMETER:{
                    return context.getSourceMethod()==null?
                        context.getSourceConstructor().getDeclaringClass().getPackage():
                        context.getSourceMethod().getDeclaringClass().getPackage();
                }
                default:{
                    throw new ParsingException("ModelParsingUtilities<findEnclosingPackage()>::\n\t" +
                            "ERROR: I was not supposed to get here!");
                }
            }
        }
        return null;
    }

    /**
     * Metoda na najdenie kontextu, do ktoreho idem pridavat informacie.
     * Tu ide o kontext v zmysle mapovania, tj to, co sa da vycitat z mapovanych
     * hodnot cielovych elementov (inak povedane co sa da vycitat aj z XML), napr.
     * skutocny kontext moze byt balik.Trieda.metoda, ale kedze pri metode
     * som nedal vytlacit tuto informaciu, z XML viem zistit iba kontext
     * balik.Trieda. Potom v zmysle kontextoveho tlacenia je potrebne napr.
     * pre parameter vytlacit aj to co chyba, tj. metoda.# a nie len cislo
     * parametra, ktore by potencialne malo stacit, lebo skutocny kontext
     * uz metodu definuje.
     * @param information
     * @return
     */
    private static String findJavaElementContext(Information information){
        while(information!=null){
            // Ak je nieco vo value, znamena to ze po tuto Information
            // je kontext uz urceny, staci mi zistit, na k akemu elementu je tato
            // informacia
            if(information.getTargetElementValue()!=null &&
                    !information.getTargetElementValue().equals("")){
                return information.getTargetQualifiedName();
            }
            information=information.getParent();
        }
        return "";
    }

    /**
     * Metoda na najdenie kotvy, podla ktorej budem urcovat, ktore anotacie sa
     * maju pri danej konfiguracii spracovat. Principialne sa hlada medzi predkami
     * informacia typu anchor.
     * @param parent
     * @param anchor
     * @return
     */
    public static Information findAnchor(Information parent, ConfigurationType anchor){
        while(parent!=null){
            if(parent.getMMConfiguration().equals(anchor)){
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Ma za ulohu prejst modelom informacii a odstranit nadbytocne informacie -
     * tie, ktore sa nemaju mapovat do modelu.
     * @param model
     * @return
     */
    public static Information removeSkipped(Information model){
        if(checkForSkippedChildren(model.getMMConfiguration())){
            // Ak su medzi priamymi potomkami nejaky nespracovavani, preskocime
            Map<ConfigurationType, List<Information>> map =
                    getChildrenToProcess(model);
            model.setChildren(map);
            for(ConfigurationType configuration : model.getChildren().keySet()){
                for(Information information : model.getChildren().get(configuration)){
                    information.setParent(model);
                }
            }
        }
        // A pokracujeme dalsimi potomkami
        for(ConfigurationType configuration : model.getChildren().keySet()){
            for(Information child : model.getChildren().get(configuration)){
                removeSkipped(child);
            }
        }
        return model;
    }

    /**
     * Testuje, ci nejaky z priamych potomkov je potrebne vynechat.
     * @param configuration
     * @return
     */
    private static boolean checkForSkippedChildren(ConfigurationType configuration){
        for(ConfigurationType child : configuration.getChildren()){
            if(child.getMappingOfConfigurationToXML().getXMLOutputType()==XMLProcessing.SKIP_PROCESS)
                return true;
        }
        return false;
    }

    /**
     * Metoda najde vsetky informacie, ktore maju byt spracovane ako
     * nasledovnici aktualnej informacie. Zoradenie sa riesi podla metamodelu.
     * @param information
     * @return
     */
    private static Map<ConfigurationType, List<Information>> getChildrenToProcess(Information information){
        // Pripravim si navratovu tabulku
        Map<ConfigurationType, List<Information>> map = new HashMap<ConfigurationType, List<Information>>();
        for(ConfigurationType child : information.getMMConfiguration().getChildren()){
            // Pra kazdeho potomka
            if(child.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS){
                // Ak ho nevynechavam, tak ho vlozim do tabulky
                map.put(child, information.getChildren().get(child));
            } else {
                if(information.getChildren().containsKey(child)){
                    // Ak preskakujem, najdem vsetky informacie tohto typu
                    for(Information infoChild : information.getChildren().get(child)){
                        // Rekurziou ziskam ich potomkov, ktori sa maju spracovat
                        Map<ConfigurationType, List<Information>> map2 = getChildrenToProcess(infoChild);
                        // A vlozim do tejto tabulky
                        for(ConfigurationType child2 : map2.keySet()){
                            if(map.containsKey(child2)){
                                map.get(child2).addAll(map2.get(child2));
                            } else {
                                map.put(child2, map2.get(child2));
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Cielom metody je prefiltrovat anotacie podla podporovanych typov a prefixov
     * cielovych elementov.
     * @param configuration
     * @param annotations
     * @return
     */
    public static Set<AnnotationTypeInstance> filterAnnotations(ConfigurationType configuration, Set<AnnotationTypeInstance> annotations){
        Set<AnnotationTypeInstance> retAnnotations = new HashSet<AnnotationTypeInstance>();
        // Odfiltrovanie podla typu
        if(configuration.getMappingOfConfigurationToSources().getSupportedSources()!=null){
            // Ak je podporovany typ definovany
            List<ElementKind> elemKinds = Arrays.asList(configuration.getMappingOfConfigurationToSources().getSupportedSources());
            if(configuration.getMappingOfConfigurationToSources().getInvertSupportedSources()){
                // Ak sa invertuje typ
                for(AnnotationTypeInstance annotationInstance : annotations){
                    if(!elemKinds.contains(annotationInstance.getJavaElementKind())){
                        // Tak aby typ cieloveho elementu presiel sitom, nesmie
                        // ho zoznam obsahovat
                        retAnnotations.add(annotationInstance);
                    }
                }
            } else {
                for(AnnotationTypeInstance annotationInstance : annotations){
                    if(elemKinds.contains(annotationInstance.getJavaElementKind())){
                        // V tomto pripade ho zoznam obsahovat musi
                        retAnnotations.add(annotationInstance);
                    }
                }
            }
        } else {
            // Aby som mohol dalej predpokladat, ze retAnnotations obsahuje
            // prefiltrovane anotacie
            retAnnotations=annotations;
        }
        annotations = new HashSet<AnnotationTypeInstance>();
        // Odfiltrovanie pre path
        if(configuration.getMappingOfConfigurationToSources().getSupportedPrefix()!=null){
            // Testovanie podla prefixov
            String[] prefixes = configuration.getMappingOfConfigurationToSources().getSupportedPrefix();
            if(configuration.getMappingOfConfigurationToSources().getInvertSupportedPrefix()){
                for(String prefix : prefixes){
                    for(AnnotationTypeInstance annotationInstance : retAnnotations){
                        if(!annotationInstance.getJavaElementCanonicalName().startsWith(prefix)){
                            // Pri invertovani, ak sa cielovy element nezacina ziadnym prefixom
                            // Kedze ide o mnozinu, neriesim duplicitu
                            annotations.add(annotationInstance);
                        }
                    }
                }
            } else {
                for(String prefix : prefixes){
                    for(AnnotationTypeInstance annotationInstance : retAnnotations){
                        if(annotationInstance.getJavaElementCanonicalName().startsWith(prefix)){
                            // A naopak
                            annotations.add(annotationInstance);
                        }
                    }
                }
            }
        } else {
            // Opat aby som mohol predpokladat ze annotations obsahuje mnozinu,
            // ktoru chcem vratit
            annotations=retAnnotations;
        }
        return annotations;
    }
}
