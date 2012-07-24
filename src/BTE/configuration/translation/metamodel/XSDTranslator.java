package BTE.configuration.translation.metamodel;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.TranslationException;
import BTE.configuration.model.metamodel.enums.TypeOfElement;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.translation.XMLWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Trieda starajuca sa o preklad metamodelu do jazyka XML schemy.
 * @author Milan
 */
public class XSDTranslator {
    // Vystupny DOM dokument
    private Document document;

    // Objekt sprostredkujuci metakonfiguraciu
    private MetaConfigurationLoader metaconfigurationLoader;

    /**
     * Konstruktor.
     * @param configuration metamodel na preklad
     * @param loader objekt sprostredkujuci metakonfiguraciu
     */
    public XSDTranslator(ConfigurationType configuration, MetaConfigurationLoader loader) {
        this.metaconfigurationLoader = loader;
        // Vytvorim dokument
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        try{
            documentBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception ex) {
            throw new TranslationException("A2XUltimate.XSDMerger<>::\n ERROR:" +
                    " Some problem while instantiating DocumentBuilder.", ex);
        }
        DOMImplementation dom = documentBuilder.getDOMImplementation();
        // Pripravim si korenovy element podla metakonfiguracie
        document = dom.createDocument(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema", null);
        Element root = this.document.getDocumentElement();
        // Prefix schemy xsd
        root.setPrefix(XSDTranslatorUtilities.PREFIX);
        // Nastavenie mennych priestorov
        root.setAttribute("targetNamespace", metaconfigurationLoader.getXMLNamespace());
        root.setAttribute("xmlns", metaconfigurationLoader.getXMLNamespace());
        root.setAttribute("elementFormDefault", "qualified");
        // Prepreklad metamodelu
        TranslatedConfiguration transConfiguration = translateAndComposeModel(configuration);
        // A jeho zlozenie do finalnej podoby
        root.appendChild(transConfiguration.getElement().cloneNode(true));
        addType(transConfiguration, new HashSet<String>());
        document.normalizeDocument();
    }

    /**
     * Vrati vygenerovanu XML schemu.
     * @param model
     * @return
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * Vypise dokument(XSD) do suboru. Zatvara za sebou outputStream.
     */
    public void outputDocument() {
        // Cesta k vystupnemu priecinku
        String path = metaconfigurationLoader.outputDirectory();
        while(path.startsWith("/")){
            // Nesmiem zacinat lomitkom
            path = path.substring(1, path.length());
        }
        // Relativna cesta k scheme
        String subPath = "";
        // Lokacia schemy
        String schemaLocation = metaconfigurationLoader.getSchemaLocationToDocument();
        while(schemaLocation.startsWith("/")){
            // Nesmiem zacinat lomitkom
            schemaLocation = schemaLocation.substring(1, schemaLocation.length());
        }
        
        if(schemaLocation.contains("/")){
            // Ak obsahuje aj priecinky, tak ich vyberiem s cielom zostavit celu cestu
            subPath = schemaLocation.substring(0, schemaLocation.lastIndexOf("/"));
        }
        
        if(path.length()>0){
            // Priprava cesty k vystupnemu priecinku
            path = path + (path.endsWith("/")?"":"/" );
        }
        // A zlozenie celej cesty
        File file = new File(path
                + subPath);
        // Vytvorenie priecinkov ak neexistuju
        file.mkdirs();

        // Cesta k samotnej scheme
        file = new File(path + schemaLocation);
        try {
            // A napokon vytvorenie a vyplnenie suboru
            file.createNewFile();
            XMLWriter.writeXMLFile(new FileOutputStream(file), document, metaconfigurationLoader.getWarningPrinter());
        } catch (IOException ex) {
            throw new TranslationException("\nBTE:\tERROR: Cannot create file \""+file.getPath()+"\".", ex);
        }
    }

    /**
     * Vystup v pripade dodania vystupneho prudu.
     * @param file
     */
    public void outputDocument(OutputStream file) {
        XMLWriter.writeXMLFile(file, document, metaconfigurationLoader.getWarningPrinter());
    }

    /**
     * Sklada XML schemu na zaklade prelozeneho metamodelu.
     * @param transConfiguration
     */
    private void addType(TranslatedConfiguration transConfiguration, Set<String> typeNames){
        if(!typeNames.contains(transConfiguration.getElementTypeName())){
            // Ak typ s danym menom este nebol pridany (musim sa vyhnut duplicite
            // napr. pri enumeracnych typoch, ktore su navratovymi hodnotami
            // viacerych vlastnosti - pre kazdy vyskyt by sa generoval novy typ s
            // rovnakym menom a vlastne celym popisom)
            typeNames.add(transConfiguration.getElementTypeName());
            // Ak existuje, pridam do dokumentu typ elementu
            if(transConfiguration.getElementType()!=null)
                document.getDocumentElement().appendChild(transConfiguration.getElementType());
            // Pomocny typ pri zlozenych typoch
            if(transConfiguration.getHelpElementType()!=null)
                document.getDocumentElement().appendChild(transConfiguration.getHelpElementType());
            // A pokracujem potomkami prelozeneho konfiguracneho typu
            for(TranslatedConfiguration child : transConfiguration.getChildren()){
                addType(child, typeNames);
            }
        }
    }

    /**
     * Metoda pre preklad a zlozenie metamodelu.
     * @param root
     * @return
     */
    private TranslatedConfiguration translateAndComposeModel(ConfigurationType root){
        TranslatedConfiguration ret = translateModel(root);
        composeModel(ret);
        return ret;
    }

    /**
     * Metoda na skladanie prelozeneho metamodelu.
     * @param root
     */
    private void composeModel(TranslatedConfiguration root){
        try {
            for(TranslatedConfiguration child : root.getChildren()){
                // Prakticky sa tu deje jednoduchy proces
                switch(child.getXmlType()){
                    case ATTRIBUTE:{
                        // Pod element schemy pre pridavanie atributov sa pridaju potomkovia - atributy
                        root.getAddAttributesElement().appendChild(child.getElement().cloneNode(true));
                        break;
                    }
                    case ELEMENT:{
                        // A pod element schemy pre pridavanie elementov sa pridaju potomkovia - elementy
                        root.getAddElementsElement().appendChild(child.getElement().cloneNode(true));
                        break;
                    }
                    default:{
                    }
                }
                // Po tom je vlastne typ elementu (atributu) pripraveny na pridanie do dokumentu
                composeModel(child);
            }
        } catch (NullPointerException ex) {
            throw new TranslationException("BTE.XSDTranslator.composeModel::\n\tERROR: "
                    + "Problem with composition of model, XSD type of some configuration"
                    + " type in metamodel must have been determined wrong, because its "
                    + "addAttributesElement or addElementsElement was not set.");
        }
    }

    /**
     * Metoda na prelozenie celeho modelu.
     * @param root
     * @return
     */
    private TranslatedConfiguration translateModel(ConfigurationType root){
        // Prelozim korenovu polozku metamodelu
        TranslatedConfiguration transRoot = translateConfiguration(root);
        for(ConfigurationType child : root.getChildrenToProcess()){
            // A kazdeho potomka, ktory ma byt spracovany
            TranslatedConfiguration transChild = translateModel(child);
            // A vytvaranie hierarchie
            transRoot.getChildren().add(transChild);
            transChild.setParent(transRoot);
        }
        return transRoot;
    }

    /**
     * Metoda prelozi jeden konfiguracny typ.
     * @param configuration
     * @return
     */
    private TranslatedConfiguration translateConfiguration(ConfigurationType configuration){
        // Navratova hodnota
        TranslatedConfiguration transConfiguration = new TranslatedConfiguration();
        // Urcim nazov, nazov typu a pocet vyskytov
        String name = configuration.getMappingOfConfigurationToXML().getName();
        String minOccurs = XSDTranslatorUtilities.resolveOccurs(configuration.getMappingOfConfigurationToXML().getMinOccurs());
        if(minOccurs!=null && minOccurs.equals("unbounded"))
            minOccurs = null;
        String maxOccurs = XSDTranslatorUtilities.resolveOccurs(configuration.getMappingOfConfigurationToXML().getMaxOccurs());
        String typeName = configuration.getMappingOfConfigurationToXML().getTypeName();
        transConfiguration.setElementTypeName(typeName);

        // Nastavim typ spracovania
        transConfiguration.setXmlType(configuration.getMappingOfConfigurationToXML().getXMLOutputType());

        // Plus typ generovania
        transConfiguration.setType(XSDTranslatorUtilities.resolveType(configuration));

        // Podla typu generujem telo konfiguracie
        switch(transConfiguration.getType()){
            case COMPLEX_EMPTY:{
                generateBodyForComplexEmpty(transConfiguration);
                break;
            }
            case COMPLEX_MIXED:{
                generateBodyForComplexMixed(transConfiguration);
                break;
            }
            case COMPLEX_SEQ:{
                generateBodyForComplexSequence(transConfiguration);
                break;
            }
            case SIMPLE:{
                generateBodyForSimple(configuration, transConfiguration);
                break;
            }
            case SIMPLE_CONTENT:{
                generateBodyForSimpleContent(configuration, transConfiguration);
                break;
            }
            case SIMPLE_CONT_RES:{
                generateBodyForSimpleContentRes(configuration, transConfiguration);
                break;
            }
        }
        // Ked uz mam telo, spracujem a pridam udaje o cielovom elemente
        processTargetElement(configuration, transConfiguration);

        // Teraz vytvorim hlavny element
        String type = null;
        switch(configuration.getMappingOfConfigurationToXML().getXMLOutputType()){
            case ATTRIBUTE:{
                type = XSDTranslatorUtilities.ATTRIBUTE;
                break;
            }
            case ELEMENT:{
                type = XSDTranslatorUtilities.ELEMENT;
                break;
            }
            default:{
                return null;
            }
        }
        // Vytvorim prislusny element s nazvom type
        Element element = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,type);
        // Nastavim jeho nazov
        element.setAttribute("name", name);
        // A typ
        element.setAttribute("type", transConfiguration.getElementTypeName());
        // Riesenie min a max Occurs
        if(configuration.getMappingOfConfigurationToXML().getXMLOutputType() == XMLProcessing.ELEMENT){
            if(minOccurs!=null)
                element.setAttribute("minOccurs", minOccurs);
            if(maxOccurs!=null)
                element.setAttribute("maxOccurs", maxOccurs);
        } else {
            // Ak je minOccurs vacsie ako 0 tak bude atribut povinny
            if((configuration.getMappingOfConfigurationToXML().getMinOccurs() >= 1) && configuration.getMappingOfConfigurationToXML().getDefaultValue()==null){
                element.setAttribute("use", "required");
            } // Nastavenie na prohibited, ak je maxOccurs == 0
            else if(configuration.getMappingOfConfigurationToXML().getMaxOccurs()==0)
            {
                element.setAttribute("use", "prohibited");
            }
        }
        // Este potrebujem zriesit predvolenu hodnotu
        if(configuration.getMappingOfConfigurationToXML().getDefaultValue()!=null){
            element.setAttribute("default", configuration.getMappingOfConfigurationToXML().getDefaultValue());
        }
        // A ulozim element do prelozenej polozky
        transConfiguration.setElement(element);
        // Teraz navratova hodnota obsahuje spravne vygenerovanu polozky pre
        // dany konfiguracny typ, ktore zatial neberie do uvahy potomkov z metamodelu,
        // o potomkov sa postara metoda composeModel
        return transConfiguration;
    }

    /**
     * Metoda generuje telo pre prazdny komplexny typ, ktory nema potomkov, iba
     * atributy (volitelne).
     * @param transConfiguration
     */
    private void generateBodyForComplexEmpty(TranslatedConfiguration transConfiguration){
        // Hlavny element pre typ - complex
        Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        // Nastavenie nazvu typu
        elementType.setAttribute("name", transConfiguration.getElementTypeName());
        transConfiguration.setElementType(elementType);
        // Atributy sa prikladaju na ten isty element - complex
        transConfiguration.setAddAttributesElement(elementType);
        // AddELements aj HelpElementType je null
    }

    /**
     * Generovanie tela pre komplexny typ s mixed obsahom.
     * @param transConfiguration
     */
    private void generateBodyForComplexMixed(TranslatedConfiguration transConfiguration){
        // Hlavny element pre typ - complex
        Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        elementType.setAttribute("name", transConfiguration.getElementTypeName());
        // Este nastavim mixed atribut
        elementType.setAttribute("mixed", "true");
        transConfiguration.setElementType(elementType);
        // Sequence element na pridavanie elementov
        Element sequence = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SEQUENCE);
        elementType.appendChild(sequence);
        // Elementy sa pridavaju do sequence
        transConfiguration.setAddElementsElement(sequence);
        // Atributy sa prikladaju na complex element
        transConfiguration.setAddAttributesElement(elementType);
        // HelpElementType je null
    }

    /**
     * Generovanie komplexneho typu so sekvenciou.
     * @param transConfiguration
     */
    private void generateBodyForComplexSequence(TranslatedConfiguration transConfiguration){
        // Hlavny element pre typ - complex
        Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        elementType.setAttribute("name", transConfiguration.getElementTypeName());
        transConfiguration.setElementType(elementType);
        // Sequence element na pridavanie elementov
        Element sequence = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SEQUENCE);
        elementType.appendChild(sequence);
        // Elementy sa pridavaju do sequence
        transConfiguration.setAddElementsElement(sequence);
        // Atributy sa prikladaju na complex element
        transConfiguration.setAddAttributesElement(elementType);
        // HelpElementType je null
    }

    /**
     * Generovanie tela pre jednoduchy typ.
     * @param configuration
     * @param transConfiguration
     */
    private void generateBodyForSimple(ConfigurationType configuration, TranslatedConfiguration transConfiguration){
        if(configuration.getMappingOfConfigurationToXSD().getTypeOfElement() == TypeOfElement.VALUE){
            // Zistim konkretny typ a nastavim zaklad typu v scheme
            String type = configuration.getMappingOfConfigurationToXSD().getSimpleTypeValue()[0];
            transConfiguration.setElementTypeName(XSDTranslatorUtilities.XSDtypes.get(type));
        } else if(configuration.getMappingOfConfigurationToXSD().getTypeOfElement() == TypeOfElement.ENUMERATED_VALUE){
            // Hlavny element pre typ - simple
            Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SIMPLE_TYPE);
            // Nastavim nazov typu
            elementType.setAttribute("name", transConfiguration.getElementTypeName());
            // Vytvorim element pre restriction
            Element restriction = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.RESTRICTION);
            // Zistim enumeracne konstanty
            String[] enumConstants = configuration.getMappingOfConfigurationToXSD().getSimpleTypeValue();
            // Nastavim ako zaklad odvodeneho typu string
            restriction.setAttribute("base", XSDTranslatorUtilities.STRING);
            for(String enumConstant : enumConstants){
                // Vytvorim element pre enumeracnu konstantu
                Element enumerationElement = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ENUMERATION);
                // A nastavim jeho hodnotu podla konstanty
                enumerationElement.setAttribute("value", enumConstant);
                // Pridam enumeracny element k restriction elementu
                restriction.appendChild(enumerationElement);
            }
            // Pripojim restriction na simpleType
            elementType.appendChild(restriction);
            // A nastavim samotny typ
            transConfiguration.setElementType(elementType);
        }
        // Vsetky elementy su null, nesmie mat ziadnych potomkov
        // AddELements, AddAttributes aj HelpElementType je null
    }

    /**
     * Metoda pre generovanie tela prelozeneho konf. typu s jednoduchym obsahom.
     * @param configuration
     * @param transConfiguration
     */
    private void generateBodyForSimpleContent(ConfigurationType configuration, TranslatedConfiguration transConfiguration){
        // Hlavny element pre typ - complex
        Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        // Nastavim nazov pomenovaneho typu
        elementType.setAttribute("name", transConfiguration.getElementTypeName());
        // Element pre simpleContent
        Element simpleContent = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SIMPLE_CONTENT);
        // Element pre extension
        Element extension = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.EXTENSION);
        // Nastavim atribut pre zaklad typu
        String type = configuration.getMappingOfConfigurationToXSD().getSimpleTypeValue()[0];
        extension.setAttribute("base", XSDTranslatorUtilities.XSDtypes.get(type));
        // Pospajam elementy
        simpleContent.appendChild(extension);
        elementType.appendChild(simpleContent);
        // Nastavim telo konfiguracie
        transConfiguration.setElementType(elementType);
        transConfiguration.setAddAttributesElement(extension);
        // AddElements a HelpELement je null
    }

    /**
     * Napokon metoda pre generovanie tela konfiguracie s jednoduchym obsahom
     * obmedzenym restriction.
     * @param configuration
     * @param transConfiguration
     */
    private void generateBodyForSimpleContentRes(ConfigurationType configuration, TranslatedConfiguration transConfiguration){
        // Vytvorim pomocny element pre extension, tiez complex
        Element helpElementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        // Nastavim jeho nazov
        helpElementType.setAttribute("name", transConfiguration.getElementTypeName()+XSDTranslatorUtilities.HELP_TYPE_NAME_POSTFIX);
        // simpleContent element pre tento element
        Element helpSimpleContent = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SIMPLE_CONTENT);
        // Dalej element extension pre simple
        Element helpExtension = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.EXTENSION);
        // Nastavim zaklad odvodeneho typu
        helpExtension.setAttribute("base", XSDTranslatorUtilities.STRING);
        // Spojim pomocny typ
        helpSimpleContent.appendChild(helpExtension);
        helpElementType.appendChild(helpSimpleContent);
        // Napojim strukturu na telo konfiguracie
        transConfiguration.setHelpElementType(helpElementType);
        transConfiguration.setAddAttributesElement(helpExtension);
        
        // A teraz hlavny typ - complex
        Element elementType = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.COMPLEX_TYPE);
        // Jeho nazov
        elementType.setAttribute("name", transConfiguration.getElementTypeName());
        Element simpleContent = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.SIMPLE_CONTENT);
        // Restriction pre enumeraciu
        Element restriction = this.document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.RESTRICTION);
        // Musim nastavit base
        restriction.setAttribute("base", transConfiguration.getElementTypeName()+XSDTranslatorUtilities.HELP_TYPE_NAME_POSTFIX);
        // Ziskam enumeracne konstanty, a pridam pre ne elementy
        String[] enumConstants = configuration.getMappingOfConfigurationToXSD().getSimpleTypeValue();
        for(String enumConstant : enumConstants){
            // Vytvorim element pre enumeracnu konstantu
            Element enumerationElement = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ENUMERATION);
            // A nastavim jeho hodnotu podla konstanty
            enumerationElement.setAttribute("value", enumConstant);
            // Pridam enumeracny element k restriction elementu
            restriction.appendChild(enumerationElement);
        }
        // Vytvorim strukturu
        simpleContent.appendChild(restriction);
        elementType.appendChild(simpleContent);
        // Doplnim do tela konfiguracie
        transConfiguration.setElementType(elementType);
        // AddElements je null
    }

    /**
     * Metoda spracuje mapovanie informacie o cielovom elemente.
     * @param configuration
     * @param transConfiguration
     */
    private void processTargetElement(ConfigurationType configuration, TranslatedConfiguration transConfiguration){
        // Atribut nemoze mat atributy/elementy, takze nema zmysel pokracovat
        if(configuration.getMappingOfConfigurationToXML().getXMLOutputType()==XMLProcessing.ATTRIBUTE){
            return;
        }
        switch(configuration.getMappingOfTargetElement().getQNameOfTargetProcView()){
            // Najprv zistim ci vobec target element mam spracovat
            case SKIP_PROCESS:{
                return;
            }
            case ATTRIBUTE:{
                // V pripade mapovania na atribut
                switch(configuration.getMappingOfTargetElement().getTargetNameType()){
                    case USER_DEFINED: {
                        // Vytvorim element schemy pre atribut
                        Element attribute = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ATTRIBUTE);
                        // Nastavim meno a typ
                        attribute.setAttribute("name", configuration.getMappingOfTargetElement().getTargetElementName());
                        attribute.setAttribute("type", XSDTranslatorUtilities.STRING);
                        // A pridam medzi atributy
                        transConfiguration.getAddAttributesElement().appendChild(attribute);
                        break;
                    }
                    case GENERIC:  {
                        ElementType[] targetElements = configuration.getMappingOfTargetElement().getTargetElements();
                        // Obdobne ako vyssie, avsak pre kazdu moznost
                        for(String name : metaconfigurationLoader.getSourceElementTypesAsStrings(targetElements)){
                            Element attribute = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ATTRIBUTE);
                            attribute.setAttribute("name", name);
                            attribute.setAttribute("type", XSDTranslatorUtilities.STRING);
                            transConfiguration.getAddAttributesElement().appendChild(attribute);
                        }
                        break;
                    }
                    default: {
                        throw new TranslationException("XSDTranslator.processTargetElement:: I was not"
                                + " supposed to get here!");
                    }
                }
                break;
            }
            case ELEMENT:{
                // V pripade mapovania na element
                switch(configuration.getMappingOfTargetElement().getTargetNameType()){
                    case USER_DEFINED: {
                        // Vytvorim element
                        Element element = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ELEMENT);
                        // Nastavim meno a typ
                        element.setAttribute("name", configuration.getMappingOfTargetElement().getTargetElementName());
                        element.setAttribute("type", XSDTranslatorUtilities.STRING);
                        // Nastavim volitelnost
                        element.setAttribute("minOccurs", "0");
                        element.setAttribute("maxOccurs", "1");
                        // A pridam medzi elementy
                        transConfiguration.getAddElementsElement().appendChild(element);
                        break;
                    }
                    case GENERIC: {
                        Element choice = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.CHOICE);
                        // Nastavim volitelnost
                        choice.setAttribute("minOccurs", "0");
                        choice.setAttribute("maxOccurs", "1");
                        ElementType[] targetElements = configuration.getMappingOfTargetElement().getTargetElements();
                        for(String name : metaconfigurationLoader.getSourceElementTypesAsStrings(targetElements)){
                            // Vytvorim element
                            Element element = document.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSDTranslatorUtilities.ELEMENT);
                            // Nastavim meno a typ
                            element.setAttribute("name", name);
                            element.setAttribute("type", XSDTranslatorUtilities.STRING);
                            // A pridam medzi elementy
                            choice.appendChild(element);
                        }
                        // Pridam choice do sekvencie
                        transConfiguration.getAddElementsElement().appendChild(choice);
                        break;
                    }
                    default: {
                        throw new TranslationException("XSDTranslator.processTargetElement:: I was not"
                                + " supposed to get here!");
                    }
                }
                break;
            }
            default:{
                return;
            }
        }
    }
}
