package BTE.configuration.translation.model;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.exceptions.TranslationException;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.translation.XMLWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Trieda zdruzujuca metody na preklad modelu do XML.
 * @author Milan
 */
public class XMLTranslator {
    // Vysledny dokument
    private Document document;

    // Implementacia sprostredkujuca metakonfiguraciu
    private MetaConfigurationLoader metaconfigurationLoader;

    /**
     * Konstruktor.
     * @param model model na preklad
     * @param loader metakonfiguracia
     */
    public XMLTranslator(Information model, MetaConfigurationLoader loader) {
        this.metaconfigurationLoader = loader;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        try{
            documentBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception ex) {
            throw new TranslationException("XMLTranslator<Constructor>::\n\tERROR:" +
                    " Some problem while instantiating DocumentBuilder.", ex);
        }
        generateModel(model, documentBuilder.getDOMImplementation());
        document.normalizeDocument();
    }

    /**
     * Vrati generovany dokument.
     * @return
     */
    public Document getDocument(){
        return this.document;
    }

    /**
     * Vypise dokument(XML) do suboru.
     */
    public void outputDocument(){
        // Cesta k vystupnemu priecinku
        String path = metaconfigurationLoader.outputDirectory();
        while(path.startsWith("/")){
            // Nesmiem zacinat lomitkom
            path = path.substring(1, path.length());
        }
        String subPath = "";

        String documentLocation = metaconfigurationLoader.getFilenameOfOutput();
        while(documentLocation.startsWith("/")){
            // Nesmiem zacinat lomitkom
            documentLocation = documentLocation.substring(1, documentLocation.length());
        }
        // Ak cesta k vystupnemu dokumentu obsahuje priecinky, tak vyberiem
        // cast s priecinkami
        if(documentLocation.contains("/")){
            subPath = documentLocation.substring(0, documentLocation.lastIndexOf("/"));
        }

        if(path.length()>0){
            // Priprava cesty k vystupnemu priecinku
            path = path + (path.endsWith("/")?"":"/" );
        }

        // Zaistim, ze existuju priecinky, ktore potrebujem
        File file = new File(path
                + subPath);
        file.mkdirs();

        // A vytvorim subor
        file = new File(path + documentLocation);
        try {
            file.createNewFile();
            XMLWriter.writeXMLFile(new FileOutputStream(file), document, metaconfigurationLoader.getWarningPrinter());
        } catch (IOException ex) {
            throw new TranslationException("\nBTE:\tERROR: Cannot create file \""+file.getPath()+"\".", ex);
        }
    }

    /**
     * Vypis do vystupneho prudu.
     * @param file
     */
    public void outputDocument(OutputStream file) {
        XMLWriter.writeXMLFile(file, document, metaconfigurationLoader.getWarningPrinter());
    }

    /**
     * Preklada model do dokumentu.
     * @param model
     */
    private void generateModel(Information model, DOMImplementation domImplementation){
        // Priprava korenoveho elementu
        document = domImplementation.createDocument(metaconfigurationLoader.getXMLNamespace(), model.getName(), null);
        Element root = document.getDocumentElement();
        // Nastavenie schemy
        root.setAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

        String schemaLocation = metaconfigurationLoader.getSchemaLocationToDocument();
        while(schemaLocation.startsWith("/"))
            schemaLocation = schemaLocation.substring(1, schemaLocation.length());
        root.setAttribute("xsi:schemaLocation", metaconfigurationLoader.getXMLNamespace()+" "+schemaLocation);
        
        // A teraz do prace s modelom
        switch(model.getXMLProcessing()){
            case ELEMENT:{
                // Predpokladam, ze korenova informacia musi byt mapovana na element
                if(model.getValue()!=null && !model.getValue().equals("")){
                    // Pridanie pripadnej hodnoty
                    root.appendChild(this.document.createTextNode(model.getValue()));
                }
                // Mapovanie cieloveho elementu
                workOutTargetElement(model, root);
                for(ConfigurationType configuration : model.getMMConfiguration().getChildrenToProcess()){
                    // Spracovanie potomkov podla typu mapovania
                    switch(configuration.getMappingOfConfigurationToXML().getXMLOutputType()){
                        case ATTRIBUTE:{
                            // Atributy
                            if(!model.getChildren().containsKey(configuration))
                                continue;
                            // Tu by bolo dobre, keby mal jediny atribut daneho nazvu, inak to spadne
                            for(Information child : model.getChildren().get(configuration))
                                root.getAttributes().setNamedItem(generateAttribute(child));
                            break;
                        }
                        case ELEMENT:{
                            if(!model.getChildren().containsKey(configuration))
                                continue;
                            // Pripajanie elementov
                            for(Information child : model.getChildren().get(configuration))
                                root.appendChild(generateBranch(child));
                            break;
                        }
                        default:{
                            throw new TranslationException("XMLTranslator.generateModel()<0>::\n\t" +
                                    "I was not supposed to get here!");
                        }
                    }

                }
                break;
            }
            default:{
                throw new TranslationException("XMLTranslator.generateModel()<1>::\n\t" +
                                    "I was not supposed to get here!");
            }
        }
    }

    /**
     * Ma z podstromu (vetvy) modelu vygenerovat strom (Vetvu) v DOM.
     * @param information
     * @return
     */
    private Element generateBranch(Information information){
        switch(information.getXMLProcessing()){
            // Vetva musi mat koren element
            case ELEMENT:{
                // Pren vytvorim element
                Element element = generateElement(information);
                for(ConfigurationType configuration : information.getMMConfiguration().getChildrenToProcess()){
                    // A spracuvam potomkov obdobne ako v metode generateModel
                    switch(configuration.getMappingOfConfigurationToXML().getXMLOutputType()){
                        case ATTRIBUTE:{
                            if(!information.getChildren().containsKey(configuration))
                                continue;
                            for(Information child : information.getChildren().get(configuration))
                                element.getAttributes().setNamedItem(generateAttribute(child));
                            break;
                        }
                        case ELEMENT:{
                            if(!information.getChildren().containsKey(configuration))
                                continue;
                            for(Information child : information.getChildren().get(configuration)){
                                element.appendChild(generateBranch(child));
                            }
                            break;
                        }
                        default:{
                            throw new TranslationException("XMLTranslator.generateBranch()<0>::\n\t" +
                                    "I was not supposed to get here!");
                        }
                    }

                }
                return element;
            }
            default:{
                throw new TranslationException("XMLTranslator.generateBranch()<1>::\n\t" +
                                    "I was not supposed to get here!");
            }
        }
    }

    /**
     * Generuje atribut pre informaciu.
     * @param information
     * @return
     */
    private Attr generateAttribute(Information information){
        if(information.getXMLProcessing()==XMLProcessing.ATTRIBUTE){
            // Ak fakt ide o atribut, tak ho vytvorim s prislusnym nazvom
            Attr node = this.document.createAttribute(information.getName());
            if(information.getValue()!=null){
                // A nastavim jeho hodnotu, ak je definovana
                node.setNodeValue(information.getValue());
                return node;
            }
            else {
                // Inak vratim null
                return null;
            }
            
        }
        return null;
    }

    /**
     * Generuje element pre informaciu (stara sa aj o cielovy element).
     * @param information
     * @return
     */
    private Element generateElement(Information information){
        if(information.getXMLProcessing()==XMLProcessing.ELEMENT){
            // Vytvorenie elementu v danom mennom priestore
            Element element = this.document.createElementNS(metaconfigurationLoader.getXMLNamespace(), information.getName());
            if(information.getValue()!=null && !information.getValue().equals("")){
                // Pridanie hodnoty
                element.appendChild(this.document.createTextNode(information.getValue()));
            }
            // Urcenie mapovania cieloveho elementu
            workOutTargetElement(information, element);
            return element;
        }
        return null;
    }

    /**
     * Stara sa o cielovy element, prida ho na element, ktory je predany ako
     * argument.
     * @param information
     * @param element
     * @return
     */
    private void workOutTargetElement(Information information, Element element){
        if(information.getTargetElementName()!=null && !information.getTargetElementName().equals("")
                && information.getTargetElementValue()!=null && !information.getTargetElementValue().equals("")){
            // Ak je co tlacit, pripravim si uzol
            Node targetElement = null;
            switch(information.getTargetElementProcessing()){
                case ATTRIBUTE:{
                    // Ak sa mapuje na atribut, vytvorim atribut
                    targetElement = this.document.createAttribute(information.getTargetElementName());
                    // A nastavim hodnotu
                    targetElement.setNodeValue(information.getTargetElementValue());
                    // A prilepim na element
                    element.getAttributes().setNamedItem(targetElement);
                    return;
                }
                case ELEMENT:{
                    // Obdobne vytvorim element
                    targetElement = this.document.createElementNS(metaconfigurationLoader.getXMLNamespace(), information.getTargetElementName());
                    // Pridam textovu hodnotu
                    targetElement.appendChild(this.document.createTextNode(information.getTargetElementValue()));
                    // A priradim k elementu
                    element.appendChild(targetElement);
                    return;
                }
                case SKIP_PROCESS:{
                    return;
                }
                default:{
                    throw new TranslationException("XMLTranslator.workOutTargetElement()::\n\t" +
                        "I was not supposed to get here!");
                }
            }
        }
    }
}
