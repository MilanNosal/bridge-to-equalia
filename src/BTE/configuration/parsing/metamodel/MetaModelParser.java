package BTE.configuration.parsing.metamodel;

import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.*;
import BTE.configuration.model.metamodel.implementation.properties.*;
import BTE.configuration.model.metamodel.implementation.ConfigurationTypeImpl;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.utilities.Utilities;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.model.metamodel.extractors.DefaultAnnotationInformationExtractor;
import BTE.configuration.model.metamodel.extractors.DefaultPropertyInformationExtractor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Tato trieda predstavuje parser zdrojovych kodov, ktory ktory na zaklade
 * metakonfiguracie generuje metamodel konfiguracie.
 * @author Milan
 */
public class MetaModelParser {
    // Objekt pre spristupnenie metakonfiguracie
    private MetaConfigurationLoader metaConfigurationLoader;

    /**
     * Konstruktor.
     * @param metaConfigurationLoader
     */
    public MetaModelParser(MetaConfigurationLoader metaConfigurationLoader) {
        this.metaConfigurationLoader = metaConfigurationLoader;
    }

    /**
     * Metoda vrati vygenerovany metamodel konfiguracie pre metakonfiguraciu
     * definovanu objektom metaConfigurationLoader.
     * @return
     */
    public ConfigurationType generateMetamodel() {
        // Start: generovanie root konfiguracie
        MappingOfConfigurationToXSD confType = new MappingOfConfigurationToXSD(
                TypeOfElement.NONE,
                new String[]{""});
        MappingOfConfigurationToSources confSource = new MappingOfConfigurationToSources(
                null,
                "",
                SourceType.NONE,
                RelativePositionToAnchor.NONE,
                null);
        MappingOfConfigurationToXML confView = new MappingOfConfigurationToXML(
                this.metaConfigurationLoader.getRootName(),
                Utilities.NAME+"-type",
                null,
                XMLProcessing.ELEMENT,
                0,
                -2,
                -2);
        MappingOfTargetElement targetElementProc = new MappingOfTargetElement();
        
        ConfigurationType root = new ConfigurationTypeImpl(
                confType,
                confSource,
                confView,
                targetElementProc,
                null);
        // End: generovanie root konfiguracie

        // Start: generovanie vrcholov pre anotacne typy
        List<Class> annTypes =
                metaConfigurationLoader.getConfigurationAnnotations();
        
        for (Class annotationType : annTypes) {
            // Ak nejde o anotacny typ, vyhodim vynimku.
            if (annotationType.isAnnotation() == false) {
                throw new ParsingException("BTE.MMParser<" +
                        annotationType.getName()+">::\n ERROR: Class " +
                        "\"" + annotationType.getName() + "\" was " +
                        "provided as configuration " +
                        "annotation, but is not an annotation type.");
            }
            generateConfForAnnType(annotationType, root);
        }
        // End: generovanie vrcholov pre anotacne typy

        return root;
    }

    /**
     * Generuje konfiguracny typ pre mapovany anotacny typ. Netyka sa pripadu,
     * ak nie je anotacny priamo konfiguracnou anotaciou, ale iba navratovym
     * typom mapovanej anotacie (t.j. napr konfiguracnej anotacie, alebo ineho
     * anotacneho typu, ktory je navratovou hodnotou nejakej vlastnosti).
     * @param clazz
     * @param parent
     * @return
     */
    protected ConfigurationType generateConfForAnnType(Class clazz, ConfigurationType parent) {
        Target target = (Target) clazz.getAnnotation(Target.class);
        ElementType[] targetElements = (target==null)?(ElementType.values()):(target.value());
        // Vytvorenie informacie o spracovani informacie o cielovom elemente
        MappingOfTargetElement targetElementMapping = new MappingOfTargetElement(
                QNameProcessing.CONTEXT_PRINT,
                XMLProcessing.ATTRIBUTE,
                null,
                TargetNameType.GENERIC,
                targetElements);

        // Generovanie informacii o zdroji konfiguracnej informacie (mapovani
        // na zdroje)
        MappingOfConfigurationToSources confSource = new MappingOfConfigurationToSources(
                clazz,
                clazz.getName(),
                SourceType.ANNOTATION,
                RelativePositionToAnchor.NONE,
                null);

        // Generovanie informacii o mapovani na XML (pouziva sa predvolene mapovanie)
        MappingOfConfigurationToXML confView = new MappingOfConfigurationToXML(
                clazz.getSimpleName(),
                Utilities.NAME+"-type",
                null,
                XMLProcessing.ELEMENT,
                0);

        // Generovanie informacii o type generovaneho elementu v XSD, ide o pomocne
        // informacie pre generovanie XSD
        MappingOfConfigurationToXSD confType = new MappingOfConfigurationToXSD(
                TypeOfElement.NONE,
                new String[]{});

        // Vytvorenie konf. typu a pridanie do stromu
        ConfigurationType confForAnnType = new ConfigurationTypeImpl(
                confType,
                confSource,
                confView,
                targetElementMapping,
                parent);
        parent.getChildren().add(confForAnnType);

        // sprostredkovanie pouzivatelskych metakonfiguracnych informacii
        confForAnnType.setMetainformations(this.metaConfigurationLoader
                .getMetaConfigurationsFor(clazz));

        confForAnnType.setMergingPoint(true);
        // Nastavenie citania anotacii
        confForAnnType.getMappingOfConfigurationToSources().setInformationExtractor(new DefaultAnnotationInformationExtractor());

        // Potrebujem usporiadat metody, pretoze implicitne usporiadanie sa
        // moze zmenit, co sposobi problemy pri porovnavani hotoveho xml
        // urobeneho podla povodneho usporiadania
        List<Method> list = Arrays.asList(clazz.getDeclaredMethods());
        Collections.sort(list, new MethodComparator());
        for(Method method : list){
            generateConfForDeclMethod(method, confForAnnType, targetElements);
        }
        return confForAnnType;
    }

    /**
     * Metoda na vygenerovanie konfiguracneho typu pre mapovanu dekl. vlastnost.
     * @param method
     * @param parent
     * @param targetElements
     * @return
     */
    protected ConfigurationType generateConfForDeclMethod(Method method, ConfigurationType parent, ElementType[] targetElements) {
        // Vytvorenie informacie o spracovani informacie o cielovom elemente
        MappingOfTargetElement targetElementProc = new MappingOfTargetElement(
                QNameProcessing.CONTEXT_PRINT,
                XMLProcessing.SKIP_PROCESS,
                null,
                TargetNameType.GENERIC,
                targetElements);
        
        // Start: Generovanie informacii o zdroji konfiguracnej informacie
        // Urcim si akeho navratoveho typu je metoda (vlastnost)
        Class returnType = method.getReturnType();
        // A typ zdroja (aky je navratovy typ vlastnosti)
        SourceType sourceType;
        sourceType = DerivationUtilities.getSourceTypeFromClass(returnType, "MMParser<"+
                Utilities.getMethodsCanonicalName(method.toGenericString(), method.getName())+">");
        
        MappingOfConfigurationToSources confSource = new MappingOfConfigurationToSources(
                method.getDeclaringClass(),
                Utilities.getMethodsCanonicalName(method.toGenericString(), method.getName()),
                sourceType,
                RelativePositionToAnchor.NONE,
                parent);
        // End: Generovanie informacii o zdroji konfiguracnej informacie

        // Start: Generovanie informacii o mapovani na XML
        // Riesenie predvolenej hodnoty
        Object defaultValue = method.getDefaultValue();
     
        // Spracujem predvolene hodnotu aby som sa vyhol nepodporovanym pripadom
        defaultValue = DerivationUtilities.parseDefaultValue(defaultValue, sourceType, method,
                "MMParser<" + Utilities.getMethodsCanonicalName(method.toGenericString(), method.getName()) + ">",
                metaConfigurationLoader.getWarningPrinter());
        MappingOfConfigurationToXML confView = new MappingOfConfigurationToXML(
                method.getName(),
                Utilities.NAME+"-type",
                (defaultValue!=null)?defaultValue.toString():null,
                XMLProcessing.ELEMENT,
                0,
                // Prave jeden vyskyt
                1, 1);
        // Pri poli vsak musim nastavit iny rozsah
        if(sourceType==SourceType.DECL_PROP_ARRAY_ANNOTATION
                || sourceType==SourceType.DECL_PROP_ARRAY_ENUM
                || sourceType==SourceType.DECL_PROP_ARRAY_PRIMITIVE
                || sourceType==SourceType.DECL_PROP_ARRAY_STRING)
        {
            confView.setMinOccurs(0);
            confView.setMaxOccurs(-1);
        }
        // End: Generovanie informacii o mapovani na XML

        // Start: Generovanie informacii o type generovaneho elementu v XSD
        // Tu su veci ako enumeracny typ a podobne.. proste dolezite detaily
        MappingOfConfigurationToXSD confType = new MappingOfConfigurationToXSD(
                DerivationUtilities.getTypeOfElementFromSourceType(sourceType),
                DerivationUtilities.getSimpleTypeValueForClass(sourceType, returnType));
        // End: Generovanie informacii o type generovaneho elementu v XSD

        ConfigurationType confForDeclaredMethod = new ConfigurationTypeImpl(
                confType,
                confSource,
                confView,
                targetElementProc,
                parent);
        parent.getChildren().add(confForDeclaredMethod);

        confForDeclaredMethod.setMetainformations(
                this.metaConfigurationLoader.getMetaConfigurationsFor(method));

        // Nastavenie citania vlastnosti
        confForDeclaredMethod.getMappingOfConfigurationToSources().setInformationExtractor(new DefaultPropertyInformationExtractor());

        // Toto ma vyriesit istu rekurziu pri generovani konf. informacii
        // pre anotacie ako vlastnosti
        if(sourceType == SourceType.DECL_PROP_ANNOTATION){
            List<Method> list = Arrays.asList(returnType.getDeclaredMethods());
            Collections.sort(list, new MethodComparator());

            for(Method annotationMethod : list){
                generateConfForDeclMethod(annotationMethod, confForDeclaredMethod, targetElements);
            }
        } else if (sourceType == SourceType.DECL_PROP_ARRAY_ANNOTATION){
            List<Method> list = Arrays.asList(returnType.getComponentType().getDeclaredMethods());
            Collections.sort(list, new MethodComparator());

            for(Method annotationMethod : list){
                generateConfForDeclMethod(annotationMethod, confForDeclaredMethod, targetElements);
            }
        }

        return confForDeclaredMethod;
    }

    /**
     * Vnutorna trieda na porovnanie dvoch metod podla mena. Potrebujem
     * mat zabezpecene deterministicke usporiadanie.
     */
    public static class MethodComparator implements Comparator<Method> {
        /**
         * Porovnanie na zaklade mien metod.
         * @param o1
         * @param o2
         * @return
         */
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
