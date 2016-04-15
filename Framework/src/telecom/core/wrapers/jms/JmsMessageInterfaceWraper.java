/*
 * Logica (c) 2008 - 2009
 */
package telecom.core.wrapers.jms;

import com.logica.eai.test.bw.jms.JmsMessage;
import com.logica.eai.test.bw.jms.JmsPropertyKey;
import telecom.core.wrapers.CommunicationMessageInterface;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Map;
import java.util.Properties;


/**
 * JmsMessage - wrapper for JMS Message.
 * 
 * @author peter.kalinak
 * @version $Id: JmsMessage.java 366 2010-05-04 08:18:13Z x0650005 $
 */
public class JmsMessageInterfaceWraper extends JmsMessage implements CommunicationMessageInterface
{

    public JmsMessageInterfaceWraper(Message msg) {
        super(msg);
    }

    public JmsMessageInterfaceWraper(String strMessage) throws JMSException {
        super(strMessage);
    }

    public JmsMessageInterfaceWraper(String strMessage, Properties jmsProperties) throws JMSException {
        super(strMessage, jmsProperties);
    }

    public JmsMessageInterfaceWraper(String strMessage, Properties jmsProperties, boolean isAdapterMessage) throws JMSException {
        super(strMessage, jmsProperties, isAdapterMessage);
    }
 /**
     * Creates JMS TextMessage with content message.
     * 
     * @param msg
     *            Content of jmsMessage.
     * @param jmsProperties
     *            properties of JMS message to set
     * @return TextMessage constructed from input string
     * @throws JMSException
     *             when setting text to JMS message fails
     */
    public  TextMessage createMessage(
            final String msg, 
            final Properties jmsProperties) throws JMSException
    {
        TextMessage textMessage = JmsFactory.getInstance().createTextMessage();
        textMessage.setText(msg);
        if (jmsProperties != null && !jmsProperties.isEmpty())
        {
            for (Map.Entry<Object, Object> entry : jmsProperties.entrySet())
            {
                if (entry.getKey() instanceof JmsPropertyKey)
                {
                    JmsPropertyKey propKey = (JmsPropertyKey) entry.getKey(); 
                    switch (propKey)
                    {
                        case JMSCorrelationID:
							textMessage.setJMSCorrelationID((String) entry.getValue());
							break;
						case JMSCorrelationIDAsBytes:
							textMessage.setJMSCorrelationIDAsBytes((byte[]) entry.getValue());
							break;
						case JMSDeliveryMode:
							textMessage.setJMSDeliveryMode((Integer) entry.getValue());
							break;
						case JMSDestination:
							textMessage.setJMSDestination((Destination) entry.getValue());
							break;
						case JMSExpiration:
							textMessage.setJMSExpiration((Long) entry.getValue());
							break;
						case JMSMessageID:
							textMessage.setJMSMessageID((String) entry.getValue());
							break;
						case JMSPriority:
							textMessage.setJMSPriority((Integer) entry.getValue());
							break;
						case JMSRedelivered:
							textMessage.setJMSRedelivered((Boolean) entry.getValue());
							break;
						case JMSReplyTo:
							textMessage.setJMSReplyTo((Destination) entry.getValue());
							break;
						case JMSTimestamp:
							textMessage.setJMSTimestamp((Long) entry.getValue());
							break;
						case JMSType:
							textMessage.setJMSType((String) entry.getValue());
							break;
						default:
                    }
                }
                else if (entry.getKey() instanceof String)
                {
                    String strKey = (String) entry.getKey();
                    String strValueType = entry.getValue().getClass().getName();
                    if ("java.lang.Boolean".equals(strValueType))
                    {
                        textMessage.setBooleanProperty(strKey, (Boolean) entry.getValue());
                    }
                    else if ("java.lang.Byte".equals(strValueType))
                    {
                        textMessage.setByteProperty(strKey, (Byte) entry.getValue());
                    }
                    else if ("java.lang.Double".equals(strValueType))
                    {
                        textMessage.setDoubleProperty(strKey, (Double) entry.getValue());
                    }
                    else if ("java.lang.Float".equals(strValueType))
                    {
                        textMessage.setFloatProperty(strKey, (Float) entry.getValue());
                    }
                    else if ("java.lang.Integer".equals(strValueType))
                    {
                        textMessage.setIntProperty(strKey, (Integer) entry.getValue());
                    }
                    else if ("java.lang.Long".equals(strValueType))
                    {
                        textMessage.setLongProperty(strKey, (Long) entry.getValue());
                    }
                    else if ("java.lang.Short".equals(strValueType))
                    {
                        textMessage.setShortProperty(strKey, (Short) entry.getValue());
                    }
                    else if ("java.lang.String".equals(strValueType))
                    {
                        textMessage.setStringProperty(strKey, (String) entry.getValue());
                    }
                    else
                    {
                        textMessage.setObjectProperty(strKey, entry.getValue());
                    }
                }
            }
        }
        return textMessage;
    }
}
