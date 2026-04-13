package utils;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.StringReader;

public class XMLValidator {
    public static boolean validate(String xmlString) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(Constants.Paths.PROTOCOL_XSD));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlString)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}