package BTE.configuration.parsing.model.annotations;

import BTE.configuration.parsing.model.annotations.wrapperhandling.WrapperHandler;
import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.communication.interfaces.AnnotationScanner;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.model.utilities.Utilities;
import BTE.configuration.parsing.model.combining.PerChildHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Trieda predstavuje parser zdrojovych kodov, na zaklade predaneho metamodelu
 * (z neho nacita mapovanie zdrojovych kodov) konfiguracie generuje model
 * konfiguracie v zdrojovych kodov.
 * @author Milan
 */
public class ModelParser {
    // Objekt sprostredkujuci metakonfiguraciu
    private final MetaConfigurationLoader metaconfigurationLoader;
    // Objekt implementujuci anotacny skener, pouziva sa na skenovanie
    // zdrojovych kodov
    private final AnnotationScanner annotationScanner;
    // Metamodel, ktory sluzi ako sablona na vytvorenie modelu
    private final ConfigurationType metaModel;

    // Vygenerovany model
    private Information model;

    /**
     * Konstruktor.
     * @param metaconfigurationLoader Objekt sprostredkujuci metakonfiguraciu
     * @param annotationScanner Objekt implementujuci anotacny skener
     * @param metaModel Metamodel, ktory sluzi ako sablona na vytvorenie modelu
     */
    public ModelParser(MetaConfigurationLoader metaconfigurationLoader, AnnotationScanner annotationScanner, ConfigurationType metaModel) {
        this.metaconfigurationLoader = metaconfigurationLoader;
        this.annotationScanner = annotationScanner;
        this.metaModel = metaModel;
    }

    /**
     * Metoda prejde metamodel, ktory dostal konstruktor ako argument a
     * vygeneruje model konfiguracie v anotaciach. Ak model vygenerovany uz bol,
     * iba vrati jeho instanciu.
     * @return
     */
    public Information parseModel(){
        if(model!=null){
            // Test, ci model je model uz vygenerovany
            return model;
        }
        // Vygeneruje korenovu informaciu a nageneruje jej potomkov
        parseChildrenOf(generateRoot());
        // Kedze kvoli konzistencii generujem aj informacie, ktore sa nemaju
        // spracovavat, po vygenerovani ich potrebujem odstranit
        
        ModelParsingUtilities.removeSkipped(model);
        // A postaram sa o expanziu wrapperov, ak je to potrebne
        if(WrapperHandler.areThereWrappersInBranch(metaModel)){
            WrapperHandler.populateWrappers(model, metaconfigurationLoader);
        }
        // A expanzia pri informaciach s vyskytom PER_CHILD
        PerChildHandler.expandInformationPerChild(model);
        return this.model;
    }

    /**
     * Metoda specialne generuje korenovu informaciu. Koren musi byt mapovany
     * na anotacny typ (pricom musi byt identifikovatelna prave jedna
     * anotacia), alebo bez mapovania a expandovania, pretoze korenova
     * informacia moze byt len jedna.
     * @return
     */
    private Information generateRoot(){
        switch(metaModel.getMappingOfConfigurationToSources().getSourceType()){
            case ANNOTATION:{
                // Pripad anotacie, musi byt prave jedna
                Set<AnnotationTypeInstance> annotations = new HashSet<AnnotationTypeInstance>(
                        this.annotationScanner.getAnnotationsOfType(metaModel.getMappingOfConfigurationToSources().getConfAnnotation().getName()));
                // A pre kazdu generujem informaciu
                if(metaModel.getMappingOfConfigurationToSources().getInformationExtractor().generate(null, null, metaModel, annotations)){
                    Object[] values = metaModel.getMappingOfConfigurationToSources().getInformationExtractor().getValues(null, null, metaModel, annotations);
                    if(!(values instanceof AnnotationTypeInstance[])){
                        throw new ParsingException("BTE.ModelParser:: ERROR:\n\tInformationExtractor for "+metaModel+" is invalid, does not return "
                                + "instances of annotations.");
                    }
                    // A vytvorim informacie pre kazdu vratenu hodnotu
                    if(values==null || values.length>1 || values.length==0){
                        throw new ParsingException("ModelParser.generateRoot()::\n\t" +
                                "There has to be exactly one annotation, that" +
                                " defines root element of XML.");
                    }
                    this.model = generateInformationForAnnotationInstance(null, metaModel, (AnnotationTypeInstance)values[0]);
                    return model;
                } else {
                    throw new ParsingException("ModelParser.generateRoot()::\n\t" +
                            "There has to be annotation, that" +
                            " defines root element of XML.");
                }
            }
            case DECL_PROP_ARRAY_ANNOTATION:
            case DECL_PROP_ARRAY_ENUM:
            case DECL_PROP_ARRAY_PRIMITIVE:
            case DECL_PROP_ARRAY_STRING:
            case DECL_PROP_ANNOTATION:
            case DECL_PROP_ENUM:
            case DECL_PROP_PRIMITIVE:
            case DECL_PROP_STRING:
            {
                // Vlastnosti vylucim
                throw new ParsingException("ModelParser.generateRoot()::\n\t" +
                            "Root element cannot be defined as declared method " +
                            "of some annotation.");
            }
            case USER_DEFINED:{
                if(metaModel.getMappingOfConfigurationToSources().getInformationExtractor().generate(null, null, metaModel, null)){
                    Object[] values = metaModel.getMappingOfConfigurationToSources().getInformationExtractor().getValues(null, null, metaModel, null);
                    for(Object value : values){
                        // Vrati prave jeden vyskyt
                        model = generateInformationForUserDefined(null, null, value.toString());
                        return model;
                    }
                }
                throw new ParsingException("ModelParser.generateRoot()::\n\tUserDefined source value was set," +
                        " but InformationExtractor provided no value to generate Information.");
            }
            case NONE:{
                // Taktiez predpokladam ze vytvaram prave jednu informaciu
                model = generateInformationForNone(null, metaModel);
                return model;
            }
            default:{
                throw new ParsingException("ModelParser.generateRoot()::\n\tI should have never gotten in here!");
            }
        }
    }

    /**
     * Cielom tejto metody je vygenerovat podstrom informacii k informacii
     * predanej ako argument.
     * @param parent
     * @return
     */
    private Map<ConfigurationType, List<Information>> parseChildrenOf(Information parent){
        // Tabulka pre vygenerovanych potomkov
        Map<ConfigurationType, List<Information>> parsedChildren = new HashMap<ConfigurationType, List<Information>>();
        // A zoznam pre potomkov jedneho typu
        List<Information> parsedChildrenOfType;
        // Postupne nagenerujem informacie pre kazdy typ potomkov
        for(ConfigurationType child : parent.getMMConfiguration().getChildren()){
            // Novy zoznam pre potomkov
            parsedChildrenOfType = new ArrayList<Information>();
            // Toto mi spracuje jeden typ potomkov
            switch(child.getMappingOfConfigurationToSources().getSourceType()){
                case ANNOTATION:{
                    // Ak ide o mapovanie na anotacny typ, najdem vhodne anotacie
                    Set<AnnotationTypeInstance> annotations = findRelevantAnnotations(parent, child);
                    // A pre kazdu generujem informaciu
                    Information anchor = ModelParsingUtilities.findAnchor(parent, child.getMappingOfConfigurationToSources().getPositionAnchor());
                    if(child.getMappingOfConfigurationToSources().getInformationExtractor().generate(anchor, parent, child, annotations)){
                        Object[] values = child.getMappingOfConfigurationToSources().getInformationExtractor().getValues(anchor, parent, child, annotations);
                        if(!(values instanceof AnnotationTypeInstance[])){
                            throw new ParsingException("BTE.ModelParser:: ERROR:\n\tInformationExtractor for "+child+" is invalid, does not return "
                                    + "instances of annotations.");
                        }
                        // A vytvorim informacie pre kazdu vratenu hodnotu
                        for(Object value : values){
                            parsedChildrenOfType.add(generateInformationForAnnotationInstance(parent, child, (AnnotationTypeInstance)value));
                        }
                    }
                    break;
                }
                case DECL_PROP_ARRAY_ANNOTATION:
                case DECL_PROP_ARRAY_ENUM:
                case DECL_PROP_ARRAY_PRIMITIVE:
                case DECL_PROP_ARRAY_STRING:
                case DECL_PROP_ANNOTATION:
                case DECL_PROP_ENUM:
                case DECL_PROP_PRIMITIVE:
                case DECL_PROP_STRING:
                {
                    // V pripade vlastnosti
                    Object sourceValue = getSourceValueForDeclaredMethod(parent, child);
                    Information anchor = ModelParsingUtilities.findAnchor(parent, child.getMappingOfConfigurationToSources().getPositionAnchor());
                    if(child.getMappingOfConfigurationToSources().getInformationExtractor().generate(anchor, parent, child, sourceValue)){
                        Object[] values = child.getMappingOfConfigurationToSources().getInformationExtractor().getValues(anchor, parent, child, sourceValue);
                        // A vytvorim informacie pre kazdu vratenu hodnotu
                        for(Object value : values){
                            parsedChildrenOfType.add(generateInformationForDeclMethod(parent, child, value));
                        }
                    }
                    break;
                }
                case USER_DEFINED:{
                    // Pouzivatelska informacia
                    // Potrebujem kotvu
                    Information anchor = ModelParsingUtilities.findAnchor(parent, child.getMappingOfConfigurationToSources().getPositionAnchor());
                    // Do spracovania vchadzam iba ak mi to potvrdi implementacia InformationExtractor-a
                    if(child.getMappingOfConfigurationToSources().getInformationExtractor().generate(anchor, parent, child, null)){
                        Object[] values = child.getMappingOfConfigurationToSources().getInformationExtractor().getValues(anchor, parent, child, null);
                        // A vytvorim informacie pre kazdu vratenu hodnotu
                        for(Object value : values){
                            parsedChildrenOfType.add(generateInformationForUserDefined(parent, child, value.toString()));
                        }
                    }
                    break;
                }
                case NONE:{
                    // A jedinu informaciu pre polozky bez mapovania na zdroje
                    parsedChildrenOfType.add(generateInformationForNone(parent, child));
                    break;
                }
                default:{
                    throw new ParsingException("ModelParser.parseChildrenOf()::\n\tI should have never gotten in here!");
                }
            }
            // Do tabulky pridam vyplneny zoznam daneho typu konfiguracnych informacii
            parsedChildren.put(child, parsedChildrenOfType);
        }
        // Napokon tabulku potomkov priradim k rodicovi
        parent.getChildren().putAll(parsedChildren);

        // A pokracujem novovytvorenymi potomkami
        for(ConfigurationType conf : parsedChildren.keySet()){
            for(Information information : parsedChildren.get(conf)){
                parseChildrenOf(information);
            }
        }
        
        return parsedChildren;
    }

    /**
     * Generuje informaciu pre konfiguracny typ, ktory nie je mapovany do zdrojov.
     * @param parent
     * @param configuration
     * @return
     */
    private Information generateInformationForNone(Information parent, ConfigurationType configuration){
        // Extrahovanie nazvu mapovania cieloveho elementu
        String targetElementName = (configuration.getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC)?
            metaconfigurationLoader.getElementKind((ModelParsingUtilities.findJavaElementActualContext(parent)==null)?null:ModelParsingUtilities.findJavaElementActualContext(parent).getJavaElementKind()):
            configuration.getMappingOfTargetElement().getTargetElementName();
        // Mapovanie cieloveho elementu
        String targetElementValue = ModelParsingUtilities.buildTargetElementValue(parent, configuration, null, null);

        // A vytvorenie informacie - cielovy element dedi z kontextu
        Information information = new InformationImpl((parent!=null)?parent.getTargetQualifiedName():"", targetElementValue, targetElementName, null,
                configuration, (parent==null)?null:parent.getInformationSource(), null);
        information.setParent(parent);
        return information;
    }

    /**
     * Metoda vygeneruje konfiguracnu informaciu pre anotaciu.
     * @param parent
     * @param configuration
     * @param annotation
     * @return
     */
    private Information generateInformationForAnnotationInstance(Information parent, ConfigurationType configuration, AnnotationTypeInstance annotation){
        // Cielovy element sa lahko ziska z AnnotationInstance objektu
        String targetElementName = (configuration.getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC)?
            metaconfigurationLoader.getElementKind(annotation.getJavaElementKind()):
            configuration.getMappingOfTargetElement().getTargetElementName();
        String targetElementValue = ModelParsingUtilities.buildTargetElementValue(parent, configuration, annotation, null);
        // Hodnota je null
        String value = null;
        // Ale ako zdroj informacii sa berie samotna anotacia
        Object sourceValue = annotation.getAnnotation();
        // Meni sa cielovy element (nededi po rodicovi)
        Information information = new InformationImpl(annotation.getJavaElementCanonicalName(), targetElementValue, targetElementName, value,
                configuration, annotation, sourceValue);
        information.setParent(parent);
        return information;
    }

    /**
     * Metoda vygeneruje konfiguracnu informaciu pre vlastnost.
     * @param parent
     * @param configuration
     * @param sourceValue
     * @return
     */
    private Information generateInformationForDeclMethod(Information parent, ConfigurationType configuration, Object sourceValue){
        String targetElementName = (configuration.getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC)?
            metaconfigurationLoader.getElementKind((ModelParsingUtilities.findJavaElementActualContext(parent)==null)?null:ModelParsingUtilities.findJavaElementActualContext(parent).getJavaElementKind()):
            configuration.getMappingOfTargetElement().getTargetElementName();
        String targetElementValue = ModelParsingUtilities.buildTargetElementValue(parent, configuration, null, null);

        String value = sourceValue.toString();
        if(configuration.getMappingOfConfigurationToSources().getSourceType()==SourceType.DECL_PROP_ANNOTATION
                || configuration.getMappingOfConfigurationToSources().getSourceType()==SourceType.DECL_PROP_ARRAY_ANNOTATION){
            // V pripade, ze hodnota je vlastne anotacia, negenerujem textovy
            // obsah, ale podstrom
            value = null;
        }
        // Kontext sa dedi od rodica
        Information information = new InformationImpl((parent!=null)?parent.getTargetQualifiedName():"", targetElementValue, targetElementName, value,
                configuration, (parent==null)?null:parent.getInformationSource(), sourceValue);
        information.setParent(parent);
        return information;
    }

    /**
     * Generuje informaciu pre pouzivatelom definovany typ konfiguracie.
     * @param parent
     * @param configuration
     * @param value
     * @return
     */
    private Information generateInformationForUserDefined(Information parent, ConfigurationType configuration, String value){
        String targetElementName = (configuration.getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC)?
            metaconfigurationLoader.getElementKind((ModelParsingUtilities.findJavaElementActualContext(parent)==null)?null:ModelParsingUtilities.findJavaElementActualContext(parent).getJavaElementKind()):
            configuration.getMappingOfTargetElement().getTargetElementName();
        String targetElementValue = ModelParsingUtilities.buildTargetElementValue(parent, configuration, null, null);
        // Cielovy element ostava po rodicovi
        Information information = new InformationImpl((parent!=null)?parent.getTargetQualifiedName():"", targetElementValue, targetElementName, value,
                configuration, (parent==null)?null:parent.getInformationSource(), null);
        information.setParent(parent);
        return information;
    }

    /**
     * Metoda vybera spravne instancie anotacii k spracovaniu.
     * @param parent
     * @param child
     * @return
     */
    private Set<AnnotationTypeInstance> findRelevantAnnotations(Information parent, ConfigurationType child){
        // Najprv pomocou anotacneho skenera ziskam vsetky anotacie daneho typu
        Set<AnnotationTypeInstance> annotations = new HashSet<AnnotationTypeInstance>(
                this.annotationScanner.getAnnotationsOfType(child.getMappingOfConfigurationToSources().getConfAnnotation().getName()));
        // Pripravim si navratovu mnozinu
        Set<AnnotationTypeInstance> returnAnnotations = new HashSet<AnnotationTypeInstance>();
        // Odfiltrujem podla nepodporovane typy a prefixy cielovych elementov
        annotations = ModelParsingUtilities.filterAnnotations(child, annotations);
        // A podla politiky prehladavania vyberiem ziadane anotacie
        // Tie sa odvijaju od uvedenej kotvy, takze hladam podla nej 
        Information anchor = ModelParsingUtilities.findAnchor(parent, child.getMappingOfConfigurationToSources().getPositionAnchor());
        switch(child.getMappingOfConfigurationToSources().getRelPositionToAnchor()){
            case NONE:{
                // Ak politika nie je definovana, vratim vsetky (samozrejme po odfiltrovani)
                return annotations;
            }
            case HIGHER_LVL:{
                String targetAnchor = anchor.getTargetQualifiedName();
                for(AnnotationTypeInstance annotation : annotations){
                    // Ak sa cele meno kotvy zacina rovnako ako cielovy element anotacie,
                    // potom je anotacia na "vyssej" urovni a pridam ho k navratovym anotaciam
                    if(targetAnchor.startsWith(annotation.getJavaElementCanonicalName())
                            && !targetAnchor.equals(annotation.getJavaElementCanonicalName())){
                        returnAnnotations.add(annotation);
                    }
                }
                return returnAnnotations;
            }
            case LOWER_LVL:{
                String targetAnchor = anchor.getTargetQualifiedName();
                for(AnnotationTypeInstance annotation : annotations){
                    // Ak sa cele meno cieloveho elementu anotacie zacina rovnako ako kotva,
                    // potom je anotacia na "nizsej" urovni
                    if(annotation.getJavaElementCanonicalName().startsWith(targetAnchor)
                            && !annotation.getJavaElementCanonicalName().equals(targetAnchor)){
                        returnAnnotations.add(annotation);
                    }
                }
                return returnAnnotations;
            }
            case SAME_HIGHER_LVL:{
                String targetAnchor = anchor.getTargetQualifiedName();
                for(AnnotationTypeInstance annotation : annotations){
                    // Ak sa cele meno kotvy zacina rovnako ako cielovy element anotacie,
                    // alebo je rovnake, potom je anotacia na "vyssej" resp.
                    // rovnakej urovni
                    if(targetAnchor.startsWith(annotation.getJavaElementCanonicalName())){
                        returnAnnotations.add(annotation);
                    }
                }
                return returnAnnotations;
            }
            case SAME_LOWER_LVL: {
                String targetAnchor = anchor.getTargetQualifiedName();
                for(AnnotationTypeInstance annotation : annotations){
                    // Ak sa cele meno cieloveho elementu anotacie zacina rovnako ako kotva,
                    // alebo je rovnake, potom je anotacia na "nizsej" alebo
                    // rovankej urovni
                    if(annotation.getJavaElementCanonicalName().startsWith(targetAnchor)){
                        returnAnnotations.add(annotation);
                    }
                }
                return returnAnnotations;
            }
            case SAME_LVL:{
                String targetAnchor = anchor.getTargetQualifiedName();
                for(AnnotationTypeInstance annotation : annotations){
                    // Ak je cele meno cieloveho elementu anotacie rovnake ako kotva,
                    // potom je anotacia na rovnakej urovni
                    if(annotation.getJavaElementCanonicalName().equals(targetAnchor)){
                        returnAnnotations.add(annotation);
                    }
                }
                return returnAnnotations;
            }
            default:{
                throw new ParsingException("ModelParser.findRelevantAnnotations()::\n\tI should have never gotten in here!");
            }
        }
    }

    /**
     * Tato metoda ma ziskat hodnotu informacie pri deklarovanych vlastnostiach anotacii.
     * @param parent
     * @param child
     * @return
     */
    private Object getSourceValueForDeclaredMethod(Information parent, ConfigurationType child){
        // Ako prve najdem instanciu kotvy, pretoze prave ta by mala obsahovat
        // objekt anotacie, ktorej vlastnost chcem prave ziskat
        Information anchor = ModelParsingUtilities.findAnchor(parent, child.getMappingOfConfigurationToSources().getPositionAnchor());
        // Z nazvu zdroja informacie vyextrahujem jednoduchy nazov vlastnosti
        String methodName = Utilities.findMethodsName(child.getMappingOfConfigurationToSources().getQualifiedNameOfSource());
        // A ziskam reflexiou metodu, ktora definuje danu vlastnost
        Method property = null;
        try {
            // Predpokladam, ze konfiguracny typ ma v odkaze mapovania
            // ulozeny anotacny typ, ktory definuje danu vlastnost
            property = child.getMappingOfConfigurationToSources().getConfAnnotation().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException ex) {
            throw new ParsingException("ModelParser.getSourceValueForDeclaredMethod()::\n\tAnnotation type "+
                    anchor.getMMConfiguration().getMappingOfConfigurationToSources().getConfAnnotation().getCanonicalName()+
                    " does not have method named "+child.getMappingOfConfigurationToSources().getQualifiedNameOfSource()+" "+methodName+".", ex);
        }
        try {
            // Ak to preslo, tak nad objektom ziskanym z kotvy volam metodu,
            // ktora definuje danu vlastnost
            return property.invoke(anchor.getSourceValue());
        } catch (Exception ex) {
            throw new ParsingException("ModelParser.getSourceValueForDeclaredMethod()::"+
                    "Exception while invoking method "+child.getMappingOfConfigurationToSources().getQualifiedNameOfSource()+" on object" +
                    ((Annotation)anchor.getSourceValue()).toString()+".", ex);
        }
    }
}
