package telecom.core.wrapers.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.logica.eai.test.bw.IntegrationRuntimeException;
import com.logica.eai.test.bw.MessageRecorder;
import com.logica.eai.test.bw.jms.JmsType;


/**
 * JMS Message Recorder. Received messages are stored. It implements
 * MessageListener interface for receiving JMS Messages.
 * 
 * @author peter.kalinak
 */
public class JmsRecorder extends MessageRecorder<JmsMessageInterfaceWraper> implements MessageListener
{
    /**
     * Logger for this class.
     */
    private static final Log LOG = LogFactory.getLog(JmsRecorder.class);

    /**
     * JMS MEssage Consumer. 
     */
    private MessageConsumer consumer = null;
    
    /**
     * Lock for synchronize recorder commands.
     */
    private Object lockObject = new Object();

    /** 
     * Destination. 
     */
    private Destination destination;
    
    /**
     * Name of destination.
     */
    private String destinationName;
    
    /**
     * JMS type.
     */
    private JmsType jmsType;
    
    /**
     * Constructor with specified destination for listening.
     * 
     * @param strDestName
     *            Destination name
     * @param targetJmsType
     *            type of object to create
     */
    public JmsRecorder(final String strDestName, final JmsType targetJmsType)
    {
        super();
        destinationName = strDestName;
        jmsType = targetJmsType;
        destination = JmsFactory.getInstance().createDestination(strDestName, jmsType);
    }

    /**
     * Constructor with specified destination for listening.
     *
     * @param dDestination
     *            new destination
     */
    public JmsRecorder(final Destination dDestination)
    {
        super();
        destination = dDestination;
    }

    /**
     * Creates listener and starts listening.
     */
    @Override
	public final void start()
    {
        consumer = JmsFactory.getInstance().createConsumer(destination);
        try
        {
            synchronized (lockObject)
            {
                consumer.setMessageListener(this);
                JmsFactory.getInstance().start();
                if(destinationName != null)
                	LOG.info("JMS Recorder for " + jmsType.toString() + ": " + destinationName
                            + " started...");
                else
                LOG.info("JMS Recorder for " /*+ jmsType.toString() + ": " + destinationName*/
                		+ destination.toString()
                        + " started...");
            }
        }
        catch (JMSException jmsExc)
        {
            throw new IntegrationRuntimeException("Problem to set listener!", jmsExc);
        }
    }

    /**
     * Stops listening.
     */
    @Override
	public final void stop()
    {
        if (consumer != null)
        {
            try
            {
                synchronized (lockObject)
                {
                    consumer.setMessageListener(null);
                    consumer.close();
                    consumer = null;
//                    LOG.info("JMS Recorder for " + jmsType.toString() + ": " + destinationName
//                            + " stopped...");
                }
            }
            catch (JMSException jmsExc)
            {
                throw new IntegrationRuntimeException("Problem to remove listener!", jmsExc);
            }
        }
    }
    
    /**
     * Restarts JMS recording.
     */
    @Override
	public final void restart()
    {
        throw new IntegrationRuntimeException("Not implemneted!");
    }

    /**
     * Listener's callback method. Received messages are acknowledged and
     * stored.
     * 
     * @param msg
     *            JMS message
     */
    public final void onMessage(final Message msg)
    {
        try
        {
            // acknowledge msg
            synchronized (lockObject)
            {
                JmsMessageInterfaceWraper message = new JmsMessageInterfaceWraper(msg);
                logMessage(message, destinationName);
                storeMsg(message);
                msg.acknowledge();
            }
        }
        catch (JMSException jmsExc)
        {
            throw new IntegrationRuntimeException("Acknowledge JMS message problem", jmsExc);
        }
    }
   
    /**
     * Returns name of queue or topic.
     * 
     * @return name of destination
     */
    public final String getDestinationName()
    {
        return destinationName;
    }
    
    public String getDest() {
    	return destination.toString();
    }

	@Override
	protected int getTimeout() {
		return 1;
	}
}
