package BTE.configuration.parsing.model.annotations.wrapperhandling;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.communication.scannotationscanner.AnnotationTypeInstanceImpl;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.parsing.model.annotations.ModelParsingUtilities;
import java.util.*;

/**
 * Trieda generujuca wrappers pre politiku PER_CLASS. Pomerne zlozita, pretoze
 * potrebujem cez reflexiu urcit spolocnu zastresujucu triedu, ktora nemusi byt
 * zrovna ta anotovana.
 * @author Milan
 */
public abstract class PerClassHandler {
    /**
     * Generuje wrappers pre triedy, pricom sa snazi prihliadat na spolocne triedy.
     * Ak sa nenajde zaobalujuca trieda pre polozku, t.j. polozka je napr. nad
     * balikom, potom sa vytvori wrapper nad kontextom rodica a jeho cielovy
     * jazykovy element sa nebude mapovat.
     * @param information rodic informacii, ktore sa maju spracovat (zastupuje wrapper, bude nahradeny)
     * @param metaconfigurationLoader
     * @return mnozina vygenerovanych wrapperov
     */
    protected static List<Information> generateWrappersPerClass(Information information, MetaConfigurationLoader metaconfigurationLoader){
        // Mnozina wrapperov
        List<Information> wrappers = new ArrayList<Information>();
        // Pripravim si mnozinu vsetkych informacii pod danym rodicom
        List<Information> children = new ArrayList<Information>();
        for(ConfigurationType childConfiguration : information.getChildren().keySet()){
            for(Information child : information.getChildren().get(childConfiguration)){
                children.add(child);
            }
        }
        
        Set<Class> commonClasses = new HashSet<Class>();
        // Kontext rodica wrapperov, jedine ak je kontext naozaj triedou
        Class context = null;
        if(ModelParsingUtilities.findJavaElementActualContext(information)!=null){
            switch(ModelParsingUtilities.findJavaElementActualContext(information).getJavaElementKind()){
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:{
                    context = ModelParsingUtilities.findJavaElementActualContext(information).getSourceClass();
                    break;
                }
                default:{
                }
            }
        }

        // Najdem ich spolocne triedy
        List<InformationHolder> holders = processClasses(children, context, commonClasses);
        children = null;

        // A tabulka pre wrappers pre rychlejsie triedenie
        Map<Class, Information> wrapperMap = new HashMap<Class, Information>();

        // Najprv napopulujem wrappers
        for(Class clazz : commonClasses){
            // Pripravim si hodnotu mapovania cieloveho jazykoveho elementu
            String value = ModelParsingUtilities.buildTargetElementValue(
                    information.getParent(),
                    information.getMMConfiguration(), (clazz==null)?information.getInformationSource():null,
                    (clazz==null)?null:clazz.getName());
            // Nazov mapovanej polozky cieloveho jazykoveho elementu
            String name;
            if(information.getMMConfiguration().getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC){
                if(clazz!=null){
                    // Ak sme nasli triedu
                    name = metaconfigurationLoader.getElementKind(ModelParsingUtilities.determineClassType(clazz));
                } else if (clazz==null && context!=null) {
                    // Ak trieda najdena nebola, a kontext je trieda
                    name = metaconfigurationLoader.getElementKind(ModelParsingUtilities.determineClassType(context));
                } else {
                    // Ak trieda najdena nebola, ale kontext nema zaobalujucu triedu (cize ma iba balik)
                    AnnotationTypeInstance ai = ModelParsingUtilities.findJavaElementActualContext(information);
                    if(ai==null){
                        name = null;
                    } else {
                        name = metaconfigurationLoader.getElementKind(ai.getJavaElementKind());
                    }
                }
            } else {
                name = information.getMMConfiguration().getMappingOfTargetElement().getTargetElementName();
            }
            // Vytvorim dummy annotationInstance, aby som mohol pracovat aj pri
            // wrapperoch aj s kontextom, staci v pripade, ze sa vytvoril novy
            // kontext
            AnnotationTypeInstance ai = null;
            if(clazz!=null) {
                ai = new AnnotationTypeInstanceImpl(clazz, ModelParsingUtilities.determineClassType(clazz), null);
            }

            // Vytvorim samotny wrapper
            Information wrapper = new InformationImpl(
                    (clazz!=null)?clazz.getName():information.getParent().getTargetQualifiedName(),
                    value, name, null, information.getMMConfiguration(), ai, null);
            wrappers.add(wrapper);
            wrapperMap.put(clazz, wrapper);
        }

        Information wrapper;
        // Prejdem teraz vsetkych potomkov
        for(InformationHolder child : holders){
            wrapper = wrapperMap.get(child.target);
            
            if(!wrapper.getChildren().containsKey(child.information.getMMConfiguration())) {
                wrapper.getChildren().put(child.information.getMMConfiguration(), new ArrayList<Information>());
            }
            // Pridame potomka na miesto
            wrapper.getChildren().get(child.information.getMMConfiguration()).add(child.information);
            
            child.information.setParent(wrapper);
        }
        return wrappers;
    }

    /**
     * Metoda pre zoznam informacii a triedu kontextu vygeneruje zoznam prislusnych
     * najviac vonkajsich spolocnych tried. Ak mam napr. cielovu triedu pre jednu informaciu
     * Trieda.VnutornaTrieda1 a pre inu Trieda.VnutornaTrieda2, tak ich spolocnou
     * najviac vonkajsou triedou je Trieda. Tymto dosiahnem zgrupovanie poloziek
     * podla najviac vonkajsej spolocnej triede.
     * @param childs
     * @param context
     * @param commonClasses mnozina spolocnych tried, do nej bude metoda pridavat novo najdene
     * @return
     */
    private static List<InformationHolder> processClasses(List<Information> childs, Class context, Set<Class> commonClasses) {
        // Priznaky vyriesenia a zasobniky tried pre kazdu konf. informaciu
        List<Deque<Class>> array = new ArrayList<Deque<Class>>(childs.size());
        boolean[] solved = new boolean[childs.size()];
        Deque<Class> stack;
        Class clazz;
        // Pre kazdu informaciu generujem stack a priznak
        for(int i = 0; i < childs.size(); i++){
            stack = new LinkedList<Class>();
            clazz = ModelParsingUtilities.findTargetClass(childs.get(i));

            if(clazz==null){
                // Ak nema zaobalujucu triedu, nemam co riesit
                solved[i] = true;
                commonClasses.add(null);
            } else {
                solved[i] = false;
            }
            // Do zasobnika vlozim vsetky triedy v hierarchii tak, ze na vrchole
            // je trieda vonkajsia, na dne najvnutornejsia, ktora bola anotovana
            while(clazz!=null){
                if(clazz.equals(context)){
                    // Ak som prisiel az ku kontextu, koncim
                    if(stack.isEmpty()){
                        // Rovnako neriesim, ak je zaobalujuca trieda zaroven kontextom
                        solved[i] = true;
                        commonClasses.add(null);
                    }
                    break;
                }
                // Idem teda od navnutornejsej po najviac vonkajsiu
                stack.push(clazz);
                clazz = clazz.getDeclaringClass();
            }
            array.add(i, stack);
        }
        List<InformationHolder> list = new ArrayList<InformationHolder>();

        // Vezmem zoznam nevyriesenych indexov
        List<Integer> indices = getUnsolved(solved);
        // Kym nemaju vsetky informacie poriesene triedy
        while(!indices.isEmpty()){
            // Vyriesim pomocou tejto metody najdeny zoznam, medzi spolocne
            // triedy pridam prave najdenu
            commonClasses.add(processAtIndices(indices, array, solved));

            indices = getUnsolved(solved);
        }
        // Nakoniec napopulovat vysledok, ocakavam prislusnu triedu na vrchole zasobnika
        // moze byt aj null v pripade prazdneho zasobnika
        for(int i = 0; i < childs.size(); i++){
            list.add(new InformationHolder(array.get(i).peek(), childs.get(i)));
        }
        return list;
    }

    /**
     * Metoda hlada najviac vonkajsiu spolocnu triedu pre co najvacsie mnoziny.
     * Zasobnik s triedami ma obsahovat na dne triedu, pre ktoru hladam najviac vonkajsiu
     * spolocnu triedu, nad nou jej vonkajsiu triedu, atd. az na vrchole je trieda
     * najvyssie v hierarchii, t.j. najviac vonkajsia trieda. Metoda pre zoznam
     * s este nevyriesenymi spolocnymi triedami vyberie prvy index v poradi,
     * vezme jeho najviac vonkajsiu triedu a hlada dalsie indexy, ktore maju rovnaku
     * triedu na vrchole. Najdeny zoznam predstavuje zoznam poloziek so spolocnou triedou,
     * pokusa sa vsak dalej odoberat polozky a testovat, pokial az budu rovnake.
     * Na vrcholoch zasobnikov tohto zoznamu napokon necha najviac vonkajsiu spolocnu
     * triedu aku nasla a oznaci v zozname priznakov o vyrieseni indexy vyriesenych.
     * @param indices mnozina indexov s este nevyriesenymi spolocnymi triedami
     * @param array mnozina LIFO zasobnikov obsahujucich triedy
     * @param solved mnozina priznakov o vyrieseni
     * @return prave najdena najviac vonkajsiu spolocna trieda
     */
    private static Class processAtIndices(List<Integer> indices, List<Deque<Class>> array, boolean[] solved){
        // Podla prvej triedy zacnem porovnavat
        Class comparing = array.get(indices.get(0)).peek();
        // Doteraz najvnutornejsia rovnaka spolocna trieda
        Class same = null;
        List<Integer> comparable = new LinkedList<Integer>();
        for(Integer i : indices){
            // Tymto vyberiem vsetky rovnajuce sa na naj - vonkajsej triede
            if(array.get(i).peek().equals(comparing)){
                comparable.add(i);
            }
        }
        // Priznak o potencialnej zmene zoznamu comparable
        boolean changed = false;
        while(!changed){
            // Spolocna trieda bude ta, ktora uz bola porovnana a potvrdena
            same = comparing;
            for(Integer i : comparable){
                // Vyberiem uz porovnane triedy
                array.get(i).pop();
            }
            // Nastavim znova porovnavanu na triedu pre prvu informaciu
            comparing = array.get(0).peek();
            // Ak som dorazil na dno, tak koncim
            // Tento test je napr. nevyhnutny, ak v comparable je len jedna
            // trieda, tj ze neexistuju dalsie informacie na tej istej
            // najviac vonkajsej triede
            if(comparing==null){
                break;
            }
            // Inak porovnavam, ci comparing nie je vnutornejsia spolocna trieda
            // pre vsetky doteraz porovnatelne
            for(Integer i : comparable){
                if(!comparing.equals(array.get(i).peek())){
                    // Ak sa nerovnaju aspon dve vnutornejsie cielove triedy,
                    // koncime, inak by sa zmensila mnozina spolocnych (chceme
                    // ju co najvacsiu)
                    changed = true;
                    break;
                }
            }
        }
        // Teraz by mal comparable obsahovat zoznam indexov s porovnanymi triedami,
        // ktore maju nejaku najviac vonkajsiu spolocnu triedu "same", polozky
        // na tych indexoch oznacim ako spracovane, a na vrcholy ich zasobnikov
        // vlozim triedu "same"
        for(Integer i : comparable){
            solved[i] = true;
            array.get(i).push(same);
        }
        return same;
    }

    /**
     * Metoda vrati zoznam indexov, na ktorych je v poli boolovskych hodnot
     * hodnota false.
     * @param array
     * @return
     */
    private static List<Integer> getUnsolved(boolean[] array){
        List<Integer> ret = new LinkedList<Integer>();
        for(int i = 0; i < array.length; i++){
            if(!array[i]){
                ret.add(i);
            }
        }
        return ret;
    }

    /**
     * Design pattern Messanger pre informacie s ich prislusnymi
     * najvnutornejsimi spolocnymi triedami.
     */
    private static class InformationHolder {
        /**
         * Obsahuje triedu, ktora predstavuje cielovy element.
         */
        private Class target;

        /**
         * A informaciu, ktorej cielovy element je definovany v target.
         */
        private Information information;

        private InformationHolder(Class target, Information information) {
            this.target = target;
            this.information = information;
        }
    }
}
