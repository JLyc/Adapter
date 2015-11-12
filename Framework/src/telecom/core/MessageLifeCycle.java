package telecom.core;

import com.tibco.tibrv.TibrvException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import telecom.config.Configuration;
import telecom.core.wrapers.CommunicationMessageInterface;
import telecom.core.wrapers.jms.JmsMessageInterfaceWraper;
import telecom.core.wrapers.rv.RVMessageInterfaceWraper;
import telecom.statistic.AdapterStatistic;

import javax.jms.JMSException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 27. 3. 2015.
 */

public class MessageLifeCycle implements Runnable {
    private static final Log LOG = LogFactory.getLog(MessageLifeCycle.class);
    private static final Map<String, String> defaultAttributes = loadDefaultAttributes();

    private static Map<String, String> loadDefaultAttributes() {
        Map<String, String> msgAttributes = new HashMap<>();
        for (String line : Configuration.getInstance().getPluginDefProp().split("\n"))
        {
            String[] nameValuePar = line.split("=");
            if(nameValuePar.length==2)
                msgAttributes.put(nameValuePar[0].trim(), nameValuePar[1].trim());
        }
        return msgAttributes;
    }

    private CommunicationMessageInterface msg;

    public MessageLifeCycle(CommunicationMessageInterface msg) {
        this.msg = msg;
    }

    /**
     * Make life cycle of message.
     * Get message, retrieve attributes for communication protocol, instantiate protocol, send attributes
     * wait for response attributes, create xml string of response attributes, create message and invoke sendReplay
     * of CommunicationClient
     */
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        CommunicationMessageInterface response=null;
        try {
            Map<String, String> msgAttributes = getMsgAttributes(msg.getText());
            Class<?> clazz = Class.forName("plugins." + msgAttributes.get("system"));
            CustomAdapterInterface ec = (CustomAdapterInterface) clazz.newInstance();
            Map<String, String> protocolResponse = ec.request(msgAttributes);
            response = createResponse(protocolResponse);

        } catch (Exception e) {
            LOG.error("Error at message thread", e);
            response = createMsg(("<body>\n\t<fail>" + e.getMessage() + "</fail>\n</body>"));
        }finally{
            CommunicationClient.getInstance().response(msg, response);
            long endTime = System.currentTimeMillis() - startTime;
            AdapterStatistic.increaseValue("ProcessingSumTime", endTime);
        }
    }

    /**
     * Parse attributes from xml text
     *
     * @param xmlString String at xml formating
     * @return Map of attributes
     * @throws IOException throws up to create fail message
     * @throws SAXException throws up to create fail message
     * @throws ParserConfigurationException throw up to create fail message
     */
    private Map<String,String> getMsgAttributes(String xmlString) throws IOException, SAXException, ParserConfigurationException {
        Map<String, String> msgAttributes = defaultAttributes;
        NodeList xmlAttributes = getXmlNodes(xmlString);

        for (int index = 0; index < xmlAttributes.getLength(); index++) {
            Node element = xmlAttributes.item(index);
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                msgAttributes.put(element.getNodeName().replaceFirst(".*:", ""), xmlAttributes.item(index).getTextContent());
            }
        }
        return msgAttributes;
    }

    private NodeList getXmlNodes(String xml) throws ParserConfigurationException, IOException, SAXException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document doc = dBuilder.parse(is);
            Element rootElement = doc.getDocumentElement();
            if (rootElement.hasChildNodes()) {
                return rootElement.getChildNodes();
            } else {
                throw new NullPointerException("Unable get Node list data from message");
            }
    }

    private CommunicationMessageInterface createResponse(Map<String, String> respond) {
        StringBuilder responseXml = new StringBuilder("<body>\n\t<success>\n");
        for (Map.Entry<String, String> entry : respond.entrySet()) {
            responseXml.append("\t\t\t<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">\n");
        }
        responseXml.append("\t</success>\n</body>");

        return createMsg(responseXml.toString());
    }

    private CommunicationMessageInterface createMsg(String responseXml){
        CommunicationMessageInterface responseMsg= null;
        try {
            if(msg instanceof JmsMessageInterfaceWraper){
                responseMsg = new JmsMessageInterfaceWraper(responseXml);
            } else if(msg instanceof RVMessageInterfaceWraper){
                responseMsg = new RVMessageInterfaceWraper(responseXml);
            }else{
                LOG.info("Unknown Message type not RV or JMS message");
            }
        } catch (JMSException |TibrvException e) {
            LOG.error("Unable to create message with response" + responseXml, e);
        }
        return responseMsg;
    }
}
