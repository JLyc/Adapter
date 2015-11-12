package plugin;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import telecom.core.CustomAdapterInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 4. 4. 2015.
 */
public class WeatherProbesTest {
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
    	requestObject.put("system", "WeatherProbes");
        requestObject.put("snmp-version","1");
//        requestObject.put("ip-address","10.180.13.131" );
        requestObject.put("ip-address","localhost" );
        requestObject.put("port","1611" );
//        requestObject.put("port","161" );
        requestObject.put("community","public" );
        requestObject.put("request-type","GET" );
//        requestObject.put("oid-value", "1.3.6.1.4.1.27070.3.4.1.2");
        requestObject.put("oid-value", "1.3.6.1.4.1.27070.3.4.1.2.1.3.1");
//        requestObject.put("oid-value", ".1.3.6.1.2.1.1.1.0");
        requestObject.put("retries-count","2" );
        requestObject.put("timeout","100" );
    }

    @Test
    public void requestTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        java.lang.Class<CustomAdapterInterface> clazz = (Class<CustomAdapterInterface>) Class.forName("com.cgi.eai.adapter.custom.telecom.plugin.SNMP.WeatherProbes");
        CustomAdapterInterface ec = clazz.newInstance();
        Map<String, String> response = null;
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.DEBUG);
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
