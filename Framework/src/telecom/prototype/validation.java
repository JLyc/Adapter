package telecom.prototype;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;import java.lang.Exception;import java.lang.String;import java.lang.System;

/**
 * Created by JLyc on 13. 4. 2015.
 */
public class validation {


    public static void main(String[] args) {
        System.out.println(validateAgainstXSD(validation.class.getClassLoader().getResourceAsStream("SNMPResponseMessage.xsd.xml"), validation.class.getClassLoader().getResourceAsStream("SNMPResponseMessage.xsd")));

    }


    static boolean validateAgainstXSD(InputStream xml, InputStream xsd)
    {
        try
        {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }
}
