package com.cgi.eai.adapter.custom.telecom.plugin.SNMP;


import com.cgi.eai.adapter.custom.telecom.plugin.SNMP.plugin_properties.WeatherProbesDefaultOID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import telecom.core.ResponseException;

import javax.xml.parsers.*;
import java.util.Map;

/**
 * Created by JLyc on 27. 3. 2015.
 */
public class WeatherProbes extends SNMPplugin {
    private static final Log LOG = LogFactory.getLog(WeatherProbes.class);

    @Override
    public Map<String, String> loadOidTranslator() {
        return WeatherProbesDefaultOID.loadOIDTranslator();
    }

    public WeatherProbes() {
    }

    /**
     * Method responsible for creating Document type response
     *
     * @param responseObject
     * @return Document or Null if exception occurs
     */
    protected Document createResponseObject(Map<String, String> responseObject) throws ResponseException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("snmp-response-message");
            doc.appendChild(rootElement);

            Element status = doc.createElement("status");
            rootElement.appendChild(status);

            Element result = doc.createElement("result");
            result.appendChild(doc.createTextNode(String.valueOf(resultCode)));
            status.appendChild(result);

            Element description = doc.createElement("description");
            description.appendChild(doc.createTextNode(descriptionText));
            status.appendChild(description);

            Element operation = doc.createElement("weatherProbe");
            rootElement.appendChild(operation);

            Attr attr = doc.createAttribute("ip");
            attr.setValue(att.get("IP-ADDRESS"));
            operation.setAttributeNode(attr);

            if (resultCode != 0) {
                return doc;
            }

            for (Map.Entry<String, String> entry : responseObject.entrySet()) {
                Element nextElement = doc.createElement(entry.getKey());
                nextElement.appendChild(doc.createTextNode(entry.getValue()));
                operation.appendChild(nextElement);
            }
            return doc;
        } catch (ParserConfigurationException e) {
            LOG.error(e.toString());
            throw new ResponseException("<snmp-response-message><status><result>2</result><description>"
                    + e.toString() + "</description></status><weatherProbe ip=\"" + att.get("IP-ADDRESS") + "\"/></snmp-response-message>");
        }
    }
}
