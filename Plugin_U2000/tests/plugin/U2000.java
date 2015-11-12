package plugin;

import org.junit.Before;
import org.junit.Test;
import telecom.core.CustomAdapterInterface;

import java.util.HashMap;
import java.util.Map;

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
        requestObject.put("system", "U2000");
        requestObject.put("ip-address","localhost" );
//        requestObject.put("ip-address","10.176.13.10" );
        requestObject.put("port","1111" );
//        requestObject.put("port","9819" );
        requestObject.put("user-name","#!5AsQrh90x7GOkwyaAJrgor/RSFdAgfpghLnq3DQeewY=" );
        requestObject.put("user-pwd","#!DGtwPAzMD9C3k/O9WXE6vVDetHQXpjjbgNdzcZvriT0=" );
        requestObject.put("retries-count","2" );
        requestObject.put("timeout","100" );
//        requestObject.put("")
    }

    @Test
    public void requestTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        java.lang.Class<CustomAdapterInterface> clazz = (Class<CustomAdapterInterface>) Class.forName("com.cgi.eai.adapter.custom.telecom.plugin.u2000.U2000");
        CustomAdapterInterface ec = clazz.newInstance();
        Map<String, String> response = null;
//        Logger root = Logger.getRootLogger();
//        root.setLevel(Level.DEBUG);
        try {
            response = ec.request(requestObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Map.Entry<String,String> res : response.entrySet()){
            System.out.println(res.getKey()+" : "+res.getValue());
        }
    }

}
