package com.cgi.eai.adapter.custom.telecom.plugin.u2000;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import telecom.core.ResponseException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * Created by JLyc on 8. 4. 2015.
 */
public class U2000 extends TL1plugin{
    private static final Log LOG = LogFactory.getLog(U2000.class);
    public U2000() throws IOException {}

    @Override
    public Document createResponseObject(Map<String, String> responseObject) throws ResponseException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("tl1-response-message");

            doc.appendChild(rootElement);

            Element status = doc.createElement("status");
            rootElement.appendChild(status);

            Element result = doc.createElement("result");
            result.appendChild(doc.createTextNode(String.valueOf(resultCode)));
            status.appendChild(result);

            Element description = doc.createElement("description");
            description.appendChild(doc.createTextNode(descriptionText));
            status.appendChild(description);


            Element operation = doc.createElement(this.att.get("OPERATION"));
            rootElement.appendChild(operation);

            if (resultCode != 0) {
                return doc;
            }

            int maxCount = 0;
            Map<String, Integer> dupliciteCount = new HashMap<>();
            for (String key : responseObject.keySet()) {
                String makeKey = key.replaceFirst("\\d+", "");
                Integer count = dupliciteCount.containsKey(makeKey)? dupliciteCount.get(makeKey) : 0;
                dupliciteCount.put(makeKey, ++count);
            }

            for(Map.Entry<String, Integer> entry : dupliciteCount.entrySet()){
                if(maxCount<entry.getValue()){
                    maxCount = entry.getValue();
                }
            }

            for (int i = 0; i < maxCount; i++) {
                Element groupElement = doc.createElement("group");
                operation.appendChild(groupElement);
                for (Map.Entry<String, Integer> entry : dupliciteCount.entrySet()) {
                    String name = entry.getKey();
                    if (maxCount > 1) {
                        name = entry.getKey()+ i;
                    }
                    if (responseObject.containsKey(name)) {
                        Element nextElement = doc.createElement(entry.getKey());
                        nextElement.appendChild(doc.createTextNode(responseObject.get(name)));
                        groupElement.appendChild(nextElement);
                    }
                }
            }
            return doc;
        }catch (Exception e){
            LOG.error(e.toString());
                throw new ResponseException("<tl1-response-message><status><result>2</result><description>"
                        +e.toString()+"</description></status><"+this.att.get("OPERATION")+"/></tl1-response-message>");
        }
    }
}
