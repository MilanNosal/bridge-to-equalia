package BTE.configuration.translation;

import BTE.configuration.communication.interfaces.IPrintStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Trieda s metodou pre zapis XML dokumentu do suboru.
 * @author Milan
 */
public abstract class XMLWriter {

    /**
     * Metoda pre vypis dokumentu do suboru.
     * @param file
     * @param document
     * @param error
     */
    public static void writeXMLFile(OutputStream file, Document document, IPrintStream error){
        try{
            Source xmlSource = new DOMSource(document);

            Result result = new StreamResult(file);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty("indent", "yes");

            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            transformer.transform(xmlSource, result);
        } catch (Exception ex){
            error.println("XMLWriter.writeXMLFile::\n\tERROR: Something went wrong while writing a file. "+ ex.getMessage());
        } finally {
            try {
                file.close();
            } catch (IOException ex) {
                error.println("XMLWriter.writeXMLFile::\n\tERROR: Something went wrong while closing a file. "+ ex.getMessage());
            }
        }
    }
}
