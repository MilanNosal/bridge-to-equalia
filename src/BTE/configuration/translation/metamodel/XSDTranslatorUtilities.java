package BTE.configuration.translation.metamodel;

import BTE.configuration.exceptions.TranslationException;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.utilities.Utilities;
import BTE.configuration.translation.enums.TranslatedConfigurationType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trieda s pomocnymi metodami a konstantami pre preklad metamodelu.
 * @author Milan
 */
public abstract class XSDTranslatorUtilities {
    // Prefix XML schemy
    public static final String PREFIX = "xsd";

    // Zoznam nazvov XSD elementov
    public static final String ELEMENT = PREFIX + ":element";
    public static final String ATTRIBUTE = PREFIX + ":attribute";
    public static final String SIMPLE_TYPE = PREFIX + ":simpleType";
    public static final String COMPLEX_TYPE = PREFIX + ":complexType";
    public static final String SIMPLE_CONTENT = PREFIX + ":simpleContent";
    public static final String SEQUENCE = PREFIX + ":sequence";
    public static final String CHOICE = PREFIX + ":choice";
    public static final String RESTRICTION = PREFIX + ":restriction";
    public static final String EXTENSION = PREFIX + ":extension";
    public static final String ENUMERATION = PREFIX + ":enumeration";

    // Datove typy v scheme
    public static final String STRING = PREFIX + ":string";
    public static final String BYTE = PREFIX + ":byte";
    public static final String SHORT = PREFIX + ":short";
    public static final String INT = PREFIX + ":int";
    public static final String LONG = PREFIX + ":long";
    public static final String FLOAT = PREFIX + ":float";
    public static final String DOUBLE = PREFIX + ":double";
    public static final String CHAR = PREFIX + ":string";
    public static final String BOOLEAN = PREFIX + ":boolean";

    public static final Map<String, String> XSDtypes;
    static {
        XSDtypes = new HashMap<String, String>();
        XSDtypes.put(char.class.getName(), CHAR);
        XSDtypes.put(String.class.getName(), STRING);
        XSDtypes.put(byte.class.getName(), BYTE);
        XSDtypes.put(short.class.getName(), SHORT);
        XSDtypes.put(int.class.getName(), INT);
        XSDtypes.put(long.class.getName(), LONG);
        XSDtypes.put(float.class.getName(), FLOAT);
        XSDtypes.put(double.class.getName(), DOUBLE);
        XSDtypes.put(boolean.class.getName(), BOOLEAN);
    }

    // Konstanta pre rozlisenie nazvu pomocneho elementu
    public static final String HELP_TYPE_NAME_POSTFIX = "-help";

    /**
     * Metoda na rozlisenie poctu vyskytov.
     * @param occurs
     * @return
     */
    public static String resolveOccurs(int occurs){
        if(occurs<-1){
            return null;
        }
        if(occurs==-1){
            return "unbounded";
        }
        if(occurs>=0){
            return occurs+"";
        }
        return null;
    }

    /**
     * Pomocna metoda by mala rozlisit z konfiguracneho typu, o aky typ
     * v scheme vlastne pojde.
     * @param configuration
     * @return
     */
    public static TranslatedConfigurationType resolveType(ConfigurationType configuration){
        // Ak to ma byt atribut, nemam inej moznosti, iba simple typ
        if(configuration.getMappingOfConfigurationToXML().getXMLOutputType()==XMLProcessing.ATTRIBUTE){
            return TranslatedConfigurationType.SIMPLE;
        }
        // Pomocny zoznam potomkov k spracovaniu
        List<ConfigurationType> childs = configuration.getChildrenToProcess();
        // Podla typu ulozenehov konfiguracnom type
        switch(configuration.getMappingOfConfigurationToXSD().getTypeOfElement()){
            case ENUMERATED_VALUE:
            {
                // Toto je vynimka, neviem pre potomkov typu element definovat
                // enumeraciu, takze sa budem tvarit ze nejde o enumeraciu
                if(((!childs.isEmpty()) && (Utilities.isThereConfigurationOfType(childs, XMLProcessing.ELEMENT)))
                        || configuration.getMappingOfTargetElement().getQNameOfTargetProcView() == XMLProcessing.ELEMENT){
                    return TranslatedConfigurationType.COMPLEX_MIXED;
                }
                // Bud SIMPLE, alebo SIMPLE_CONT_RES
                // Aby som vybral spravne, musim zistit ci ma nejake atributy
                // 1. ak mam spracovat informaciu o cielovom elemente, tak potom
                // bude mat atribut(element nemoze byt, ked uz ide o enumeraciu,
                // totiz ide o restriction, a pri nej nemozem pridavat elementy)
                if(configuration.getMappingOfTargetElement().getQNameOfTargetProcView() != XMLProcessing.SKIP_PROCESS){
                    return TranslatedConfigurationType.SIMPLE_CONT_RES;
                }
                // 2. Inak idem prezriet potomkov a zistit ich spracovanie
                if((!childs.isEmpty()) && (Utilities.isThereConfigurationOfType(childs, XMLProcessing.ATTRIBUTE))){
                    return TranslatedConfigurationType.SIMPLE_CONT_RES;
                }
                // 3. Inak ide o SIMPLE
                return TranslatedConfigurationType.SIMPLE;
            }
            case VALUE:{
                // Bud SIMPLE, alebo SIMPLE_CONTENT
                // Obdobne ako vo vyssom pripade.
                if(((!childs.isEmpty()) && (Utilities.isThereConfigurationOfType(childs, XMLProcessing.ELEMENT)))
                        || configuration.getMappingOfTargetElement().getQNameOfTargetProcView() == XMLProcessing.ELEMENT){
                    return TranslatedConfigurationType.COMPLEX_MIXED;
                }
                if(configuration.getMappingOfTargetElement().getQNameOfTargetProcView() != XMLProcessing.SKIP_PROCESS){
                    return TranslatedConfigurationType.SIMPLE_CONTENT;
                }
                if((!childs.isEmpty()) && (Utilities.isThereConfigurationOfType(childs, XMLProcessing.ATTRIBUTE))){
                    return TranslatedConfigurationType.SIMPLE_CONTENT;
                }
                return TranslatedConfigurationType.SIMPLE;
            }
            case NONE:{
                // Bud COMPLEX_EMPTY, alebo COMPLEX_SEQ
                // Zistim ci obsahuje nejakych potomkov, ktori maju byt spracovani
                // ako elementy
                // To moze byt napr. spracovanie cieloveho elementu
                if(configuration.getMappingOfTargetElement().getQNameOfTargetProcView() == XMLProcessing.ELEMENT){
                    return TranslatedConfigurationType.COMPLEX_SEQ;
                }
                // Alebo moze byt medzi konfiguracnymi potomkami
                if((!childs.isEmpty()) && (Utilities.isThereConfigurationOfType(childs, XMLProcessing.ELEMENT))){
                    return TranslatedConfigurationType.COMPLEX_SEQ;
                }
                // Ak teda napokon nema ziadnych potomkov v podobe elementov,
                // mozme ho oznacit za tzv. prazdny element
                return TranslatedConfigurationType.COMPLEX_EMPTY;
            }
            default:{
                throw new TranslationException("XSDTranslatorUtilities.resolveType()::\n\t" +
                        "I was not supposed to get here!");
            }
        }
    }

}
