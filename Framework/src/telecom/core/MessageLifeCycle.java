package telecom.core;

import com.cgi.eai.adapter.custom.telecom.config.NameValue;
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
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageLifeCycle implements Runnable {
    private static final Log LOG = LogFactory.getLog(MessageLifeCycle.class);
    private static final Map<String, String> defaultAttributes = loadDefaultAttributes();

    /**
     * Load default attributes from config file
     * @return
     */
    private static Map<String, String> loadDefaultAttributes(){
        Map<String, String> msgAttributes = new HashMap<>();
        for (NameValue line : Configuration.getInstance().getPluginDefProp())
        {
            msgAttributes.put(line.getName().toUpperCase(), line.getValue());
        }
        LOG.debug(msgAttributes);
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
            if(msgAttributes.isEmpty() || msgAttributes.get("SYSTEM")==null){
                throw new ClassNotFoundException("No plugin class found. "+msgAttributes.get("SYSTEM"));
            }
            LOG.debug(msgAttributes.get("SYSTEM"));
            Class<?> clazz = Class.forName(msgAttributes.get("SYSTEM"));
            CustomAdapterInterface ec = (CustomAdapterInterface) clazz.newInstance();
            Document protocolResponse = ec.request(msgAttributes);
            response = createResponseFromDoc(protocolResponse);

        } catch (ResponseException e) {
            LOG.error("Error at message thread but sending response", e);
            try {
                response = createMsg((e.getMessage()));
            } catch (ResponseException e1) {
                LOG.error(e1.getMessage());
            }
        } catch (IOException | InstantiationException | ParserConfigurationException | IllegalAccessException |
                SAXException | NullPointerException | ClassNotFoundException e) {
            LOG.error("Error at message thread", e);
        } catch (Exception e){
            LOG.error("Bad behaviour, review source code for Class MessageLifeCycle");
        } finally{
            CommunicationClient.getInstance().response(msg, response);
            long endTime = System.currentTimeMillis() - startTime;
            AdapterStatistic.increaseTime("ProcessingSumTime", endTime);
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
        Map<String, String> msgAttributes = new HashMap<>(defaultAttributes);
        NodeList xmlAttributes = getXmlNodes(xmlString);
        for (int index = 0; index < xmlAttributes.getLength(); index++) {
            Node element = xmlAttributes.item(index);
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                msgAttributes.put(element.getNodeName().replaceFirst(".*:", "").toUpperCase(), xmlAttributes.item(index).getTextContent());
            }
        }
        return msgAttributes;
    }

    /**
     * Parse and get Nodes from xml
     * @param xml to get Nodes from (config file)
     * @return NodeList
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
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

    /**
     * Construct response from xml Document object return by plugin.
     * @param doc Document returned from plugin
     * @return message in request message type
     */
    private CommunicationMessageInterface createResponseFromDoc(Document doc) throws NullPointerException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return createMsg(writer.toString());
        } catch (TransformerException  e) {
            e.printStackTrace();
            LOG.error("Unable to parse response DOM object");
            throw new NullPointerException("Unable to parse response DOM object");
        } catch (ResponseException e) {
            LOG.error(e.getMessage());
            throw new NullPointerException(e.getMessage());
        }
    }

    /**
     * Create message of corresponding type (JMS or RV) based on request message request type
     * @param responseXml xml document to send as message
     * @return message of request type
     */
    private CommunicationMessageInterface createMsg(String responseXml) throws ResponseException {
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
            throw new ResponseException("Unable to create response msg object");
        }
        return responseMsg;
    }
}
