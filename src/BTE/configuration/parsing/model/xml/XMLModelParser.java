package BTE.configuration.parsing.model.xml;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.RelativePositionToAnchor;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Trieda poskytuje implementaciu spracovania XML dokumentu a generovanie
 * modelu konfiguracie.
 * @author Milan
 */
public class XMLModelParser {
    // Korenovy element dokumentu
    private Element xml;

    // Metamodel
    private ConfigurationType metaModel;

    // Generovany model
    private Information model;

    // Implementacia sprostredkovania metakonfiguracie
    private MetaConfigurationLoader metaconfigurationLoader;

    /**
     * Konstruktor.
     * @param metaModel metamodel
     * @param loader sprostredkovatel metakonfiguracie
     * @param documentStream vstupny datovy prud s dokumentom
     */
    public XMLModelParser(ConfigurationType metaModel, MetaConfigurationLoader loader, InputStream documentStream) {
        this.metaModel = metaModel;
        metaconfigurationLoader = loader;
        // Otvorenie dokumentu
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        try{
            documentBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception ex) {
            throw new ParsingException("XMLModelParser<Constructor>::\n\tERROR:" +
                    " Some problem while instantiating DocumentBuilder.", ex);
        }
        Document document;
        try {
            if(documentStream==null){
                this.xml = null;
            } else {
                document = documentBuilder.parse(documentStream);
                this.xml = document.getDocumentElement();
            }
        } catch (SAXException ex) {
            throw new ParsingException("XMLModelParser<Constructor>::\n\tERROR:" +
                    " Some problem while parsing one of the input documents.", ex);
        } catch (IOException ex){
            metaconfigurationLoader.getWarningPrinter().println("XMLModelParser<Constructor>::\n\tWARNING:" +
                    " Trying to open one of the input files, but it " +
                    "does not exist or is corrupted.");
            this.xml = null;
        }
    }

    /**
     * Vrati model informacii z xml.
     * @return
     */
    public Information getModel(){
        if(model==null){
            if(!isOfType(xml, metaModel)){
                return null;
            }

            model = parseXML(xml, metaModel, "");
        }
        return model;
    }

    /**
     * Ma spracovat podstrom xml, ktory obsahuje konfiguracne informacie a vracia
     * vygenerovany podstrom informacii.
     * @param xml koren DOM dokumentu
     * @param type typ korena podstromu
     * @param actualTarget kontext cieloveho elementu podstromu
     * @return
     */
    private Information parseXML(Element xml, ConfigurationType type, String actualTarget){
        if(xml==null){
            return null;
        }
        // Zoznam potomkov mapovanych na elementy
        List<Element> childs = new ArrayList<Element>();
        NodeList nodeList = xml.getChildNodes();
        for(int i = 0; i<nodeList.getLength(); i++){
            if(nodeList.item(i).getNodeType()==Node.ELEMENT_NODE){
                // Pridavam iba elementy
                childs.add((Element)nodeList.item(i));
            }
        }
        
        // Vygenerujem informaciu pre koren, po tomto bude childs obsahovat iba
        // potomkov, uz nie mapovanie cieloveho elementu
        Information information = generateInformation(xml, type, actualTarget, childs);
        // Nastavim aktualny kontext
        actualTarget = information.getTargetQualifiedName();
        
        // Spracujem informacie, ktore su mapovane na atributy v xml pre koren
        generateAttributesForInformation(information, xml, type, actualTarget);
        // Pokusim sa roztriedit potomkov korenoveho elementu podla mapovania
        // na konfiguracne typy, childs uz neobsahuje mapovanie cieloveho elementu
        Map<ConfigurationType, List<Element>> children = getChildrenByTypes(type, childs);

        List<Information> list;
        for(ConfigurationType configuration : children.keySet()){
            // Zoznam pre dany typ potomkov
            list = new ArrayList<Information>();
            for(Element element : children.get(configuration)){
                // A potomkov vygenerujem rekurzivne
                Information child = parseXML(element, configuration, actualTarget);
                list.add(child);
                child.setParent(information);
            }
            information.getChildren().put(configuration, list);
        }
        return information;
    }

    /**
     * Pokusi sa identifikovat atributy, ktore ma informacia mat ako nasledovnikov
     * v podstrome a pripoji ich k danej informacii.
     * @param information
     * @param element
     * @param type
     * @param targetElement
     * @return
     */
    private Information generateAttributesForInformation(Information information, Element element, ConfigurationType type, String targetElement){
        for(ConfigurationType child : type.getChildrenToProcess()){
            // Spracujem kazdy konfiguracny typ potomka mapovany na atribut
            if(child.getMappingOfConfigurationToXML().getXMLOutputType()!=XMLProcessing.ATTRIBUTE)
                continue;
            // Ziskam hodnotu atributu
            String value = element.getAttribute(child.getMappingOfConfigurationToXML().getName());
            if(!value.equals("")){
                // Ak atribut existuje, vygenerujem pren informaciu a pridam k rodicovi
                List<Information> list = new ArrayList<Information>();
                Information child2 = new InformationImpl(targetElement, null, null, value, child);
                child2.setParent(information);
                list.add(child2);
                information.getChildren().put(child, list);
            }
        }
        return information;
    }

    /**
     * Generuje informaciu pre element. Snazi sa identifikovat
     * element/atribut pre target element. Meni zoznam potomkov childs, vynecha
     * mapovanie cieloveho elementu.
     * @param element DOM element spracovavany na informaciu
     * @param type konfiguracny typ informacie
     * @param targetElementQName kontext informacie bez ohladu na jej cielovy element
     * @param childs zoznam elementov, ktore predstavuju potomkov alebo mapovanie cieloveho elementu
     * @return
     */
    private Information generateInformation(Element element, ConfigurationType type, String targetElementQName, List<Element> childs){
        // Premenna pre hodnotu
        String value = null;
        // Zoznam potomkov
        NodeList nList = element.getChildNodes();
        // Urcim hodnotu
        for(int i = 0; i < nList.getLength(); i++){
            // Zaroven vsak nechcem nejake formatovacie znaky
            if(nList.item(i).getNodeType()== Node.TEXT_NODE
                    && !nList.item(i).getNodeValue().trim().equals("")){
                // Urcenie hodnoty ako textoveho uzla s neprazdnym obsahom
                value = nList.item(i).getNodeValue();
            }
        }
        // Ak nemam definovany cielovy element, koncim, niet viac co riesit
        if(type.getMappingOfTargetElement().getQNameOfTargetProcView()==XMLProcessing.SKIP_PROCESS){
            return new InformationImpl(targetElementQName, null, null, value, type);
        }

        // Inak najdem cielovy element, prva polozka dvojice obsahuje nazov
        // mapovanej informacie, druha samotnu hodnotu
        // Metoda zaroven odstrani najdene mapovanie cieloveho elementu zo zoznamu
        Twins<String> target = findTargetElement(element, type, childs);
        
        switch(type.getMappingOfTargetElement().getQNameOfTargetProcType()){
            case CONTEXT_PRINT:{
                // Ak je nasledujuci element na vyssej urovni
                if(target.first==null){
                    // Ak nie je najdene mapovanie, teda kontext sa nemeni
                } else if(targetElementQName.startsWith(target.second)){
                    // Inak ak sa kontext zmenil smerom ku korenu stromu zdr. kodov
                    targetElementQName = target.second;
                } else if(type.getMappingOfConfigurationToSources().getRelPositionToAnchor()==RelativePositionToAnchor.NONE){
                    // Predpoklad, ze nejde o kontextovy vypis
                    targetElementQName = target.second;
                } else {
                    // Skladanie kontextu do aktualneho cieloveho elementu
                    targetElementQName = targetElementQName + "." + target.second;
                }
                break;
            }
            case FULL_PRINT:{
                if(target.first!=null){
                    // Pri celom vypise je zmena cieloveho elementu jasna
                    targetElementQName = target.second;
                }
                break;
            }
            case SIMPLE_PRINT:{
                if(target.first!=null){
                    // Ak je uvedeny
                    if(targetElementQName.endsWith(target.second)){
                        // ak je to aktualny, tak nic neriesim (tiez nie celkom
                        // deterministicke)
                    }
                    // Ak sa da predpokladat, ze ide o vyssi level
                    else if((type.getMappingOfConfigurationToSources().getRelPositionToAnchor()==RelativePositionToAnchor.HIGHER_LVL
                            || type.getMappingOfConfigurationToSources().getRelPositionToAnchor()==RelativePositionToAnchor.SAME_HIGHER_LVL)
                            && targetElementQName.contains(target.second)) {
                        // Tak sa pokusim orezat to co je naviac
                        targetElementQName = targetElementQName.substring(0, targetElementQName.lastIndexOf(target.second))+target.second;
                    } else {
                        // Inak budem velmi optimisticky a pridam ku koncu kontextu informaciu
                        // ziskanu z xml
                        targetElementQName = targetElementQName+"."+target.second;
                    }
                }
                
                break;
            }
            default:{
                throw new ParsingException("XMLModelParser<generateInformation()>::\n\tError: I was" +
                            "not supposed to get here!");
            }
        }
        // A vytvorim novu informaciu
        // Treba pamatat, ze urcovanie kontextu je je dost nedeterministicke,
        // momentalne vsak porovnavanie pri spajani vychadza cisto z hodnot,
        // ktore su ziskane priamo z xml, nie celeho kontextu, ktory tu takto
        // tazkopadne skladam, takze nemal by vzniknut vazny problem
        return new InformationImpl(targetElementQName, target.second, target.first, value, type);
    }

    /**
     * Hlada hodnotu z mapovanej polozky pre cielovy element danej informacie
     * reprezentovanej DOM elementom.
     * @param element
     * @param type
     * @param childsOfElement zoznam elementov, ktory vystupuju ako potomkovia, z neho odstranim mapovanie cieloveho elementu, ak najdem
     * @return
     */
    private Twins<String> findTargetElement(Element element, ConfigurationType type, List<Element> childsOfElement){
        // Ak sa mapuje na atribut
        if(type.getMappingOfTargetElement().getQNameOfTargetProcView()==XMLProcessing.ATTRIBUTE){
            switch(type.getMappingOfTargetElement().getTargetNameType()){
                // Rozlisujem podla mena
                case GENERIC:{
                    ElementType[] targetElements = type.getMappingOfTargetElement().getTargetElements();
                    for(String name : metaconfigurationLoader.getSourceElementTypesAsStrings(targetElements)){
                        // A testujem kazdy mozny nazov atributu (ak nenajdem ziadny,
                        // vraciam dvojicu null, null). Tiez isty nedeterminizmus
                        // v pripade, ze informacia ma potomkov, ktori sa mapuju
                        // na atribut s nazvom, aky je v zozname moznych mapovani
                        // cieloveho elementu
                        String value = element.getAttribute(name);
                        if(!value.equals("")){
                            return new Twins<String>(name, value);
                        }
                    }
                    break;
                }
                case USER_DEFINED:{
                    // Pri pouzivatelom definovanom mapovani nazvu je to jasne
                    String value = element.getAttribute(type.getMappingOfTargetElement().getTargetElementName());
                    if(!value.equals("")){
                        return new Twins<String>(type.getMappingOfTargetElement().getTargetElementName(), value);
                    }
                    break;
                }
                default:{
                    throw new ParsingException("XMLModelParser<findTargetElement()>::\n\tError: I was" +
                            "not supposed to get here!");
                }
            }
        } else // Pri mapovani na element je to trosku zlozitejsie
        if (type.getMappingOfTargetElement().getQNameOfTargetProcView()==XMLProcessing.ELEMENT){
            switch(type.getMappingOfTargetElement().getTargetNameType()){
                // Budem predpokladat ze mapovani cielovy element je prvy v
                // poradi, myslim ze to mozem, kedze v scheme davam sequence
                case GENERIC:{
                    // NodeList list = element.getChildNodes();
                    if(childsOfElement.size()>0){
                        String name, value;
                        Element child = childsOfElement.get(0);

                        // Podla moznych pouziti informacie
                        ElementType[] targetElements = type.getMappingOfTargetElement().getTargetElements();
                        // Vyberam mozne genericke nazvy
                        for(String name2 : metaconfigurationLoader.getSourceElementTypesAsStrings(targetElements)){
                            if(child.getLocalName().equals(name2)){
                                // Ak najdem zhodu
                                name = name2;
                                if(child.getFirstChild().getNodeType()!=Node.TEXT_NODE){
                                    // Ak nejde vsak element, o ktorom som predpokladal,
                                    // ze je mapovanim cieloveho elementu, ma obsah
                                    // rozny od textu, tak potom to nie je mapovanie
                                    // cieloveho elementu
                                    return new Twins<String>(null, null);
                                }
                                // Inak vytiahnem hodnotu a vratim aktualnu dvojicu
                                value = child.getFirstChild().getNodeValue().trim();
                                // Odstranim element ako spracovany
                                childsOfElement.remove(child);
                                return new Twins<String>(name, value);
                            }
                        }
                    }
                    return new Twins<String>(null, null);
                }
                case USER_DEFINED:{
                    if(childsOfElement.size()>0){
                        String value;
                        Element child = childsOfElement.get(0);

                        if(child.getLocalName().equals(type.getMappingOfTargetElement().getTargetElementName())){
                            // Ak je zhoda
                            if(child.getFirstChild().getNodeType()!=Node.TEXT_NODE){
                                // Ak nejde vsak element, o ktorom som predpokladal,
                                // ze je mapovanim cieloveho elementu, ma obsah
                                // rozny od textu, tak potom to nie je mapovanie
                                // cieloveho elementu
                                return new Twins<String>(null, null);
                            }
                            // Inak vytiahnem hodnotu a vratim aktualnu dvojicu
                            value = child.getFirstChild().getNodeValue().trim();
                            // Odstranim element ako spracovany
                            childsOfElement.remove(child);
                            return new Twins<String>(type.getMappingOfTargetElement().getTargetElementName(), value);
                        }
                    }
                    return new Twins<String>(null, null);
                }
                default:{
                    throw new ParsingException("XMLModelParser<findTargetElement()>::\n\tError: I was" +
                            "not supposed to get here!");
                }
            }
        }
        // V akomkolvek inom pripade vraciam dvojicu null, null
        return new Twins<String>(null, null);
    }

    /**
     * Metoda ma porovnat element s typom a urcit ci element
     * je daneho typu.
     * @param element
     * @param type
     * @return
     */
    private boolean isOfType(Element element, ConfigurationType type){
        if(element==null)
            return false;
        // Porovnavam iba na zaklade nazvu, co nemusi byt jednoznacne, avsak
        // je to dostatocne presne a rychle pre prakticke problemy
        if(!element.getLocalName().equals(type.getMappingOfConfigurationToXML().getName())){
            return false;
        }
        return true;
    }

    /**
     * Snazi sa rozlisit priame nasledovnicke elementy podla sekvencie a priradit
     * im tak ich typy. Vracia roztriedenu tabulky elementov podla typov potomkov.
     * @param type
     * @param childsOfElement zoznam potomkov, nemal by uz obsahovat pripadne mapovanie cieloveho elementu
     * @return
     */
    private Map<ConfigurationType, List<Element>> getChildrenByTypes(ConfigurationType type, List<Element> childsOfElement) {
        // Navratova hodnota
        Map<ConfigurationType, List<Element>> retMap = new HashMap<ConfigurationType, List<Element>>();
        // Zoznam elementov pre polozku tabulky
        List<Element> elements = new ArrayList<Element>();
        // Iterator pre zoznam spracovavanych typov potomkov
        Iterator<ConfigurationType> list = type.getChildrenToProcess().iterator();
        // Prave ocakavany typ elementu
        ConfigurationType expected = null;
        do{
            // Vyberiem dalsi typ
            if(list.hasNext()){
                expected = list.next();
            }
            else {
                return retMap;
            }
            // Ale hladam taky, ktory sa mapuje na element
        } while(expected.getMappingOfConfigurationToXML().getXMLOutputType()!=XMLProcessing.ELEMENT);

        // Premenna pre prave rozlisovany element
        Element xmlElement;

        for(int i = 0; i < childsOfElement.size(); ){
            xmlElement = childsOfElement.get(i);
            // Zistujem ci sa nemeni ocakavany typ
            if(isOfType(xmlElement, expected)){
                // Ak je element ocakavaneho typu, pridam ho do zoznamu
                elements.add(xmlElement);
                // A posuniem sa na dalsi prvok zoznamu
                i++;
            } else {
                // Ak nie je, znamena to ze v sekvencii som sa dostal na dalsi
                // typ
                // Ulozim do tabulky prave rozliseny typ
                retMap.put(expected, elements);
                // Najdem dalsi ocakavany typ elementov
                do{
                    if(list.hasNext()){
                        expected = list.next();
                    }
                    else {
                        // Ak dalsi nie je, koncim a vraciam co som uz ziskal
                        return retMap;
                    }
                } while (expected.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.ELEMENT);

                // Vytvorim novy zoznam pre dalsi typ
                elements = new ArrayList<Element>();
            }
        }
        // Ak mi dosli polozky zoznamu, koncim a vkladam posledny rozlisovany
        // typ do tabulky
        retMap.put(expected, elements);
        // A vraciam vysledok
        return retMap;
    }
}
