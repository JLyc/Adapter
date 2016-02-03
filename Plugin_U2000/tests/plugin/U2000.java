package plugin;

import org.junit.*;
import org.w3c.dom.*;
import telecom.core.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;

/**
 * Created by CGIusr on 11/9/15.
 */
public class U2000 {
    private Map<String, String> requestObject = new HashMap<>();

    /*      "<con:snmp-request-message xmlns:con=\"http://www.cgi.com/eai/adapter/custom/telecom/config\">\n" +
            "  <con:>0</con:snmp-version>\n" +
            "  <con:ip-address>127.0.0.1</con:ip-address>\n" +
            "  <con:port>161</con:port>\n" +
            "  <con:community>public</con:community>\n" +
            "  <con:request-type>SET</con:request-type>\n" +
            "  <con:oid-value>1.3.6.1.2.148</con:oid-value>\n" +
            "  <con:retries-count>2</con:retries-count>\n" +
            "  <con:timeout>100</con:timeout>\n" +
            "</con:snmp-request-message>";
    */

    @Before
    public void prepareRequest() {
        requestObject.put("SYSTEM", "U2000");
        requestObject.put("IP-ADDRESS", "localhost");
//        requestObject.put("ip-address","10.176.13.10" );
        requestObject.put("PORT", "1112");
//        requestObject.put("port","9819" );
        requestObject.put("USER-NAME", "#!5AsQrh90x7GOkwyaAJrgor/RSFdAgfpghLnq3DQeewY=");
        requestObject.put("USER-PWD", "#!DGtwPAzMD9C3k/O9WXE6vVDetHQXpjjbgNdzcZvriT0=");
        requestObject.put("RETRIES-COUNT", "2");
        requestObject.put("TIMEOUT", "100");

        requestObject.put("ALIAS", "48575443EBFAA923");
//        requestObject.put("ALIAS","48575443237A5329");
//        requestObject.put("ALIAS","32303131E487BA41");
        requestObject.put("OPERATION","getStatus");
        requestObject.put("OPERATION","setSIP");
        requestObject.put("SIPINDEX","1");
        requestObject.put("SIPPWD","1234");
//        requestObject.put("operation", "getConfig");
//        requestObject.put("operation", "getUpstream");

    }

    @Test
    public void requestTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (int index = 0; index <= 0; index++) {
            java.lang.Class<CustomAdapterInterface> clazz = (Class<CustomAdapterInterface>) Class.forName("com.cgi.eai.adapter.custom.telecom.plugin.u2000.U2000");
            CustomAdapterInterface ec = clazz.newInstance();
            Document response = null;
            try {
                response = ec.request(requestObject);
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                DOMSource source = new DOMSource(response);

                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
                System.out.println(writer.toString());
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }

}
