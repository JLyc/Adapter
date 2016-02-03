package plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.junit.*;
import org.w3c.dom.*;
import telecom.core.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;

/**
 * Created by JLyc on 4. 4. 2015.
 */
public class WeatherProbesTest {
    private static final Log LOG = LogFactory.getLog( WeatherProbesTest.class);
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
    	requestObject.put("SYSTEM", "WeatherProbes");
        requestObject.put("SNMP-VERSION","1");
        requestObject.put("IP-ADDRESS","10.180.13.131" );
//        requestObject.put("IP-ADDRESS","localhost" );
        requestObject.put("PORT","161" );
        requestObject.put("COMMUNITY","public" );
        requestObject.put("OID-MAX","30" );
        requestObject.put("REQUEST-TYPE","GET" );
        requestObject.put("OID-NAME", "1.3.6.1.4.1.27070.3.4.1.2");
        requestObject.put("OID-NAME", "getStatus");
//        requestObject.put("OID-NAME", "1.3.6.1.4.1.27070.3.4.1.2.1.3.1");
//        requestObject.put("OID-NAME", ".1.3.6.1.2.1.1.1");
//        requestObject.put("OID-NAME", ".1.3.6.1.2.1.1");
        requestObject.put("OIDDEPTH", "4");
        requestObject.put("RETRIES-COUNT","2" );
        requestObject.put("TIMEOUT","100" );
    }

    @Test
    public void requestTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        java.lang.Class<CustomAdapterInterface> clazz = (Class<CustomAdapterInterface>) Class.forName("com.cgi.eai.adapter.custom.telecom.plugin.SNMP.WeatherProbes");
        CustomAdapterInterface ec = clazz.newInstance();
        Document response = null;
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.DEBUG);
        try {
            System.out.println(requestObject);
            response = ec.request(requestObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for(Map.Entry<String,String> res : response.entrySet()){
//            System.out.println(res.getKey()+" : "+res.getValue());
//        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(response);

            // Output to console for testing
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
