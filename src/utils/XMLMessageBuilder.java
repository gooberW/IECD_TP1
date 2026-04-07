package utils;

import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XMLMessageBuilder {
    // No utils/XMLMessageBuilder.java
    public static String toString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            // 1. Remover a declaração <?xml...?> (Obrigatório para o nosso Scanner)
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            // 2. Garantir que não há indentação (várias linhas)
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            // 3. REMOÇÃO AGRESSIVA de quebras de linha e espaços extras
            return writer.getBuffer().toString().trim().replaceAll("[\\r\\n]+", "");
        } catch (Exception e) {
            return null;
        }
    }
}