package BTE.configuration.parsing.metamodel;

import BTE.configuration.communication.interfaces.IPrintStream;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.SourceType;
import BTE.configuration.model.metamodel.enums.TypeOfElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Trieda zoskupujuca metody, ktore sluzia pri generovani metamodelu z
 * anotacnych typov.
 * @author Milan
 */
public abstract class DerivationUtilities {
    /**
     * Metoda sluzi na kontrolu a pripravu predvolenej
     * hodnoty vlastnosti anotacneho typu.
     * @param defaultValue
     * @param sourceType
     * @param method
     * @return
     */
    public static Object parseDefaultValue
            (Object defaultValue, SourceType sourceType, Method method,
            String sourceOfCall, IPrintStream warningPrinter){
        // Niektore pripady vsak logicky nebudem podporovat
        if(defaultValue != null){
            if(sourceType == SourceType.DECL_PROP_ANNOTATION){
                printWarning("BTE.DerivationUtilities."+sourceOfCall+
                        "::\n WARNING: Default value is not supported when " +
                        "the return type is an annotation.", warningPrinter);
                defaultValue = null;
            } else
            // Vylucit potrebujem este array, necham iba ak je default s
            // jedinou polozkou
            if(defaultValue.getClass().isArray()){

                // Pripad, ze pole ma viac ako jeden komponent
                if(Array.getLength(defaultValue) > 1){
                    printWarning("BTE.DerivationUtilities."+sourceOfCall+
                            "::\n WARNING: Default value is not supported when " +
                            "the return type is an array with more than 1 " +
                            "component.", warningPrinter);
                    defaultValue = null;
                }   // Pripad ked pole nema ziaden komponent, potom vlastne nie
                // je definovana predvolena hodnota
                else if(Array.getLength(defaultValue) == 0){
                    defaultValue = null;
                } else {
                    Class clazz = Array.get(defaultValue, 0).getClass();
                    // Este zabezpecim, ze nejde o pole anotacii
                    if(clazz.isAnnotation()){
                        printWarning("BTE.DerivationUtilities."+sourceOfCall
                                +"::\n WARNING: Default value is not supported " +
                                "when the return type is an array of annotations.", warningPrinter);
                        defaultValue = null;
                    } else {
                        // Ak vsetko prebehlo az sem, mozem vybrat hodnotu
                        // jedineho komponentu
                        defaultValue = Array.get(defaultValue, 0);
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * Metoda pre rozlisenie typu zdroja informacie podla triedy, pouziva sa
     * na rozlisenie typu navratovej hodnoty deklarovanej vlastnosti.
     * @param clazz
     * @return
     */
    public static SourceType getSourceTypeFromClass(Class clazz, String sourceOfCall){
        SourceType sourceType;
        if(clazz.isAnnotation()){
            // Navratovy typ je anotacny typ
            sourceType = SourceType.DECL_PROP_ANNOTATION;
        } else if(clazz.isArray()){
            // Pole
            Class componentClass = clazz.getComponentType();
            if(componentClass.isAnnotation()){
                // Pole anotacii
                sourceType = SourceType.DECL_PROP_ARRAY_ANNOTATION;
            } else if(componentClass.isEnum()){
                // Pole enumeracnych konstant
                sourceType = SourceType.DECL_PROP_ARRAY_ENUM;
            } else if(componentClass.equals(String.class)){
                // Pole retazcov
                sourceType = SourceType.DECL_PROP_ARRAY_STRING;
            } else {
                // Ak nejde o pole primitivnych hodnot (teda uz iba Class ostava)
                if(!componentClass.isPrimitive()){
                    // Tak vynimka, nepodporujem
                    throw new ParsingException("BTE.DerivationUtilities."+sourceOfCall+
                            "::\n ERROR: Unsupported return type of" +
                            " declared method. "+clazz.getCanonicalName());
                }
                // Pole primitivneho typu
                sourceType = SourceType.DECL_PROP_ARRAY_PRIMITIVE;
            }
        } else if(clazz.isEnum()){
            // Enumeracna konstanta
            sourceType = SourceType.DECL_PROP_ENUM;
        } else if(String.class.equals(clazz)){
            // Retazec
            sourceType = SourceType.DECL_PROP_STRING;
        } else {
            if(!clazz.isPrimitive()){
                throw new ParsingException("BTE.DerivationUtilities."+sourceOfCall+
                        "::\n ERROR: Unsupported return type of" +
                        "declared method.");
            }
            // A napokon primitivny typ
            sourceType = SourceType.DECL_PROP_PRIMITIVE;
        }
        return sourceType;
    }

    /**
     * Pre dany typ mapovania na zdroje SourceType urcim prisluchajuci
     * typ elementu v XSD TypeOfElement.
     * @param sourceType
     * @return
     */
    public static TypeOfElement getTypeOfElementFromSourceType(SourceType sourceType){
        switch(sourceType){
            case ANNOTATION:
                // Anotacia je predvolene iba s potomkami (elementy/atributy)
                return TypeOfElement.NONE;
            case DECL_PROP_ANNOTATION:
                // Rovnako ak ide o dklarovanu vlastnost s navratovou hodnotou anotacneho typu
                return TypeOfElement.NONE;
            case DECL_PROP_ARRAY_PRIMITIVE:
                // V pripade primitivnych typov zakladny typ v XSD
                return TypeOfElement.VALUE;
            case DECL_PROP_ARRAY_ANNOTATION:
                return TypeOfElement.NONE;
            case DECL_PROP_ARRAY_ENUM:
                // Specialne pre definovanie enumeracie
                return TypeOfElement.ENUMERATED_VALUE;
            case DECL_PROP_ARRAY_STRING:
                return TypeOfElement.VALUE;
            case DECL_PROP_ENUM:
                return TypeOfElement.ENUMERATED_VALUE;
            case DECL_PROP_PRIMITIVE:
                return TypeOfElement.VALUE;
            case DECL_PROP_STRING:
                return TypeOfElement.VALUE;
            case NONE:
                return TypeOfElement.NONE;
            default:
                return TypeOfElement.NONE;
        }
    }

    /**
     * Ulohou metody je poskytnut pole retazcov, urcujuce jednoduchy typ
     * v xml scheme. Podstatne pre enumeracny typ (zoznam enumeracnych
     * konstant), pri primitivnych typoch a String je to ich nazov, ziskany pomocou
     * metody getName(). Pouziva sa na vyber vhodneho typu z XSD typov. Ak nejde
     * o enumeracny typ, mal by vraciat iba jediny prvok.
     * @param sourceType
     * @param clazz
     * @return
     */
    public static String[] getSimpleTypeValueForClass(SourceType sourceType, Class clazz){
        List<String> ret = new ArrayList<String>();
        // Zacnem enumeracnym typom, v tom pripade tam vlozim enumeracne konstanty
        if(sourceType == SourceType.DECL_PROP_ENUM && clazz.isEnum()){
            for(Object enumConstant : clazz.getEnumConstants()){
                ret.add(enumConstant.toString());
            }
        } // Specialne pre array, pretoze je iny sposob ziskania typu komponentov
        else if(sourceType == SourceType.DECL_PROP_ARRAY_ENUM && clazz.isArray() && clazz.getComponentType().isEnum()){
            for(Object enumConstant : clazz.getComponentType().getEnumConstants()){
                ret.add(enumConstant.toString());
            }
        }   // Dalsi bude pripad retazca
        else if((sourceType == SourceType.DECL_PROP_STRING && clazz.equals(String.class))
                || (sourceType == SourceType.DECL_PROP_ARRAY_STRING && clazz.isArray() && clazz.getComponentType().equals(String.class))){
            ret.add(String.class.getName());
        } // Ak ide o anotacie, resp. NONE, tak nema zmysel nejako ziskavat jednoduchy typ
        else if(sourceType == SourceType.ANNOTATION || sourceType == SourceType.DECL_PROP_ANNOTATION || sourceType == SourceType.NONE){
            ret.add("");
        } // A napokon pre primitivne typy. Tiez netreba zabudnut na rozdielny sposob ziskania pri poli
        else if((sourceType == SourceType.DECL_PROP_PRIMITIVE && clazz.isPrimitive())
                || (sourceType == SourceType.DECL_PROP_ARRAY_PRIMITIVE && clazz.isArray() && clazz.getComponentType().isPrimitive())){
            if(clazz.isArray()){
                ret.add(clazz.getComponentType().getName());
            } else {
                ret.add(clazz.getName());
            }
        }
        return ret.toArray(new String[]{});
    }

    /**
     * Privatna metoda na vypis varovani.
     * @param cause
     */
    private static void printWarning(String cause, IPrintStream warningPrinter){
        warningPrinter.println(cause);
    }
}
