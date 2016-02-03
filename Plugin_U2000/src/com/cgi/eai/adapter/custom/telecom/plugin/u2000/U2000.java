package com.cgi.eai.adapter.custom.telecom.plugin.u2000;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * Created by JLyc on 8. 4. 2015.
 */
public class U2000 extends TL1plugin{

    public U2000() throws IOException {}

    @Override
    protected Document createResponseObject(Map<String, String> responseObject) {
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

            if(resultCode!=0){
                return doc;
            }

            Element operation = doc.createElement(this.att.get("OPERATION"));
            rootElement.appendChild(operation);



            int counter = 1;
            int maxCount = 0;
            TreeSet<String> treeSet = new TreeSet<>();
            for (String key : responseObject.keySet()) {
                if (!treeSet.add(key.replaceFirst("\\d", ""))) {
                    if (counter++ > maxCount) {
                        maxCount = counter;
                    }
                } else {
                    counter = 1;
                }
            }

            for (int i = 0; i <= maxCount; i++) {
                Element groupElement = doc.createElement("group");
                operation.appendChild(groupElement);
                for (String key : treeSet) {
                    String name = key;
                    if (maxCount > 0) {
                        name = key + i;
                    }
                    if (responseObject.containsKey(name)) {
                        Element nextElement = doc.createElement(key);
                        nextElement.appendChild(doc.createTextNode(responseObject.get(name)));
                        groupElement.appendChild(nextElement);
                    }
                }
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
