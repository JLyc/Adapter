package telecom.core.wrapers.jms;

import com.cgi.eai.adapter.custom.telecom.config.JmsDescriptor;
import com.logica.eai.test.bw.MessagePublisher;
import com.logica.eai.test.bw.jms.JmsMessage;
import com.logica.eai.test.bw.jms.JmsPropertyKey;
import com.logica.eai.test.bw.jms.JmsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.config.Configuration;
import telecom.core.AdapterCore;
import telecom.core.CommunicationClientInterface;
import telecom.core.wrapers.CommunicationInterface;
import telecom.core.wrapers.CommunicationMessageInterface;

import javax.jms.*;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public class JmsCommunication implements CommunicationInterface {
    private static final Log LOG = LogFactory.getLog(JmsCommunication.class);

    // Configuration instance
    private static Configuration cfg = Configuration.getInstance();
    // JMS fields
    private JmsRecorder jmsRecorder = null;
    private JmsDescriptor jmsDescriptor = cfg.getJmsDescriptor();
    private boolean isListening = true;
    private CommunicationClientInterface communicationClient;

    public JmsCommunication(CommunicationClientInterface communicationClient) {
        this.communicationClient = communicationClient;
    }

    public void init() {
        LOG.info("Initializing JMS communication ...");
        try {
            JmsFactory.getInstance().initAsQueueHandler(jmsDescriptor);
            jmsRecorder = new JmsRecorder(cfg.getSubjectDescriptor().getSubject(), JmsType.QUEUE);
            jmsRecorder.start();
            LOG.info("JMS Listening on subject: " + cfg.getSubjectDescriptor().getSubject());
        } catch (NamingException | JMSException e) {
            LOG.error("JmsFactory failed to initialize", e);
            AdapterCore.shutDown(1704);
        }
    }

    public void close() {
        LOG.info("Closing JMS connection....");
        jmsRecorder.stop();
        JmsFactory.getInstance().stop();
        JmsFactory.getInstance().close();
    }

    @Override
    public void stopListening() {
        isListening = false;
    }

    @Override
    public void sendReply(CommunicationMessageInterface responseMsg, CommunicationMessageInterface sourceMsg) {
        MessageProducer producer = null;
        try {
            Destination destination = JmsFactory.getInstance().createDestination(Configuration.getInstance().getJmsResponseSubject().getResponseSubject(), JmsType.QUEUE);
            producer = JmsFactory.getInstance().createProducer(destination);
            Message response = ((JmsMessageInterfaceWraper) responseMsg).getMessage();
            response.setJMSCorrelationID(((JmsMessage) sourceMsg).getMessage().getJMSCorrelationID());
            response.setJMSType(((JmsMessage) sourceMsg).getMessage().getJMSType());
            producer.send(response);
            LOG.info("Reply sent to QUEUE " + destination);
        } catch (JMSException e) {
            LOG.error("Unable reply on message", e);
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    LOG.error("Error on closing reply produces", e);
                }
            }
        }

    }

    @Override
    public void run() {
        while (isListening) {
            JmsMessageInterfaceWraper msg = (JmsMessageInterfaceWraper) jmsRecorder.getMsg(5);
            if (msg != null) {
                LOG.debug("Sending message to executor");
                communicationClient.request(msg);
            }
        }
        LOG.info("JMS listener stopped");
    }
}
