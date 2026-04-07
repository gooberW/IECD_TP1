package utils;

import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XMLMessageBuilder {
    public static String toString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            // tira a declaracao da versao do xml para nao lixar o scanner
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            //tira todas as quebras de linha para evitar problemas
            return writer.getBuffer().toString().trim().replaceAll("[\\r\\n]+", "");
        } catch (Exception e) {
            return null;
        }
    }
}