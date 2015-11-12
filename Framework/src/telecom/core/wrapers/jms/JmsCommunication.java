package telecom.core.wrapers.jms;

import com.cgi.eai.adapter.custom.telecom.config.JmsDescriptor;
import com.logica.eai.test.bw.jms.JmsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.config.Configuration;
import telecom.core.AdapterCore;
import telecom.core.CommunicationClientInterface;
import telecom.core.wrapers.CommunicationInterface;
import telecom.core.wrapers.CommunicationMessageInterface;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.naming.NamingException;

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
        try {
            sendReply((JmsMessageInterfaceWraper) responseMsg, (JmsMessageInterfaceWraper) sourceMsg);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        init();

        while (isListening) {
            JmsMessageInterfaceWraper msg = jmsRecorder.getMsg(1);
            if (msg != null) {
                communicationClient.request(msg);
            }
        }
        LOG.info("JMS listener stopped");
    }

    private void sendReply(final JmsMessageInterfaceWraper replyMsg, final JmsMessageInterfaceWraper msg)
            throws JMSException {
        Destination destination = msg.getMessage().getJMSReplyTo();
        if (destination != null) {
            MessageProducer producer = null;
            try {
                producer = JmsFactory.getInstance().createProducer(destination);
                producer.send(replyMsg.getMessage());
                LOG.info("Reply sent to QUEUE " + destination);
            } finally {
                if (producer != null) {
                    producer.close();
                }
            }
        } else {
            LOG.error("No destination to reply to...");
        }
    }
}
