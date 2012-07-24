package BTE.configuration.model.utilities;

import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Trieda zaobalujuca pomocne metody pre pracu s modelom a metamodelom
 * konfiguracie.
 * @author Milan
 */
public abstract class Utilities {
    /**
     * Pattern, ktory predstavuje nazov konfiguracneho typu.
     */
    public static final String NAME = "${name}";

    /**
     * Metoda ma prejst zoznam konfiguracnych typov a zistit, ci sa medzi nimi
     * nachadza aspon jedna, ktora ma nastaveny typ mapovania na argument type.
     * @param configurations
     * @param type
     * @return
     */
    public static boolean isThereConfigurationOfType(List<ConfigurationType> configurations, XMLProcessing type){
        for(ConfigurationType configuration : configurations){
            if(configuration.getMappingOfConfigurationToXML().getXMLOutputType() == type){
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda modifikuje model tak, aby bola zohladnena priorita v poradi
     * elementov.
     * @param model
     * @return
     */
    public static ConfigurationType orderModelByOrderPriority(ConfigurationType model){
        for(ConfigurationType configuration : model.getChildren()){
            orderModelByOrderPriority(configuration);
        }
        Collections.sort(model.getChildren(), new ConfigurationTypeComparator());
        return model;
    }

    /**
     * Metoda nahradza vzory v retazci skutocnymi hodnotami.
     * @param stringToResolve
     * @param involvedConf
     * @return
     */
    private static String stringResolve(String stringToResolve, ConfigurationType involvedConf){
        // Nahrada ${name}
        String retString = stringToResolve.replace(Utilities.NAME, involvedConf.getMappingOfConfigurationToXML().getName());

        return retString;
    }

    /**
     * Metoda ma upravit textovu hodnoty poli konfiguracneho typu, ktore mozu
     * obsahovat patterns.
     * @param model
     * @return
     */
    public static ConfigurationType replaceCodes(ConfigurationType model){
        // Momentalne iba nazov typu
        model.getMappingOfConfigurationToXML().setTypeName(stringResolve(model.getMappingOfConfigurationToXML().getTypeName(), model));
        for(ConfigurationType configuration : model.getChildren()){
            replaceCodes(configuration);
        }
        return model;
    }

    /**
     * Metoda, ktora z generickeho popisu metody (metoda toGenericString())
     * ziska cely nazov metody (vratane zoznamu parametrov) a teda vlastne
     * jedinecny popis metody. Cielom je ponechat cely nazov, ale zaroven odrezat
     * klauzulu throws a modifikatory. Odporucam pouzivat na ziskanie identifikacie
     * metody pri parsovani, ale aj pri citani z vysledneho modelu konfiguracie.
     * @param methodGenericName
     * @param methodName
     * @return
     */
    public static String getMethodsCanonicalName(String methodGenericName, String methodName){
        // Principialne iba z generickeho nazvu vyberiem token, ktory obsahuje
        // jednoduche meno metody, tym orezem modifikatory a throws klauzulu
        StringTokenizer st = new StringTokenizer(methodGenericName, " ");
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(token.contains(methodName)){
                return token;
            }
        }
        return methodName;
    }

    /**
     * Vrati z celeho mena metody jednoduche meno metody. Potrebujem
     * pri citani deklarovanych vlastnosti anotacii za pouzitia reflexie.
     * @param fullMethodsName - nazov ziskany pomocou Utilities.getMethodsCanonicalName
     * @return
     */
    public static String findMethodsName(String fullMethodsName){
        String retVal = fullMethodsName.substring(0, fullMethodsName.indexOf("("));
        retVal = fullMethodsName.substring(retVal.lastIndexOf(".")+1, fullMethodsName.indexOf("("));
        return retVal;
    }

    /**
     * Pomocna trieda pre usporiadanie konfiguracnych typov podla priority
     * poradia.
     */
    private static class ConfigurationTypeComparator implements Comparator<ConfigurationType> {
        /**
         * Porovna 2 konfiguracne typy, vracia kladnu hodnotu ak o1 ma byt v
         * poradi za o2, zapornu ak o1 ma byt pred o2 a 0 ak sa ich poradie
         * nema zmenit.
         * @param o1
         * @param o2
         * @return
         */
        public int compare(ConfigurationType o1, ConfigurationType o2) {
            return o2.getMappingOfConfigurationToXML().getOrderPriority() - o1.getMappingOfConfigurationToXML().getOrderPriority();
        }
    }
}
