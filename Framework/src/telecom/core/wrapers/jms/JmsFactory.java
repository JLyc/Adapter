//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package telecom.core.wrapers.jms;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cgi.eai.adapter.custom.telecom.config.JmsDescriptor;
import com.logica.eai.test.bw.IntegrationRuntimeException;
import com.logica.eai.test.bw.jms.JmsType;

public class JmsFactory {
    private static final Log LOG = LogFactory.getLog(JmsFactory.class);
    private static JmsFactory instance = null;
    private InitialContext ctx;
    private Connection jmsConnection = null;
    private Session jmsSession = null;

    public JmsFactory() {
    }

    public static synchronized JmsFactory getInstance() {
        if(instance == null) {
            instance = new JmsFactory();
        }

        return instance;
    }

    private void init(JmsDescriptor descriptor, JmsType jmsType) throws NamingException, JMSException {
        if(descriptor != null) {
            LOG.info("initialization of JMS...");
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", descriptor.getContext());
            env.put("java.naming.provider.url", descriptor.getUrl());
            if(descriptor.getJndiLogin() != null) {
                env.put("java.naming.security.principal", descriptor.getJndiLogin());
            }

            if(descriptor.getJndiPassword() != null) {
                env.put("java.naming.security.credentials", descriptor.getJndiPassword());
            }
            ctx = new InitialContext(env);
            ConnectionFactory factory = 
                    (ConnectionFactory) ctx.lookup(
                        JmsType.QUEUE.equals(jmsType) 
                            ? descriptor.getQueueFactory() : descriptor.getTopicFactory()
                    );
            if(descriptor.getLogin() != null) {
                this.jmsConnection = factory.createConnection(descriptor.getLogin(), descriptor.getPassword());
            } else {
                this.jmsConnection = factory.createConnection();
            }

            this.jmsSession = this.jmsConnection.createSession(false, 1);
        } else {
            LOG.info("JMS server not specified in configuration, skipping initialization...");
        }

    }

    public final void initAsQueueHandler(JmsDescriptor descriptor) throws NamingException, JMSException {
        this.init(descriptor, JmsType.QUEUE);
    }

    public final void initAsTopicHandler(JmsDescriptor descriptor) throws NamingException, JMSException {
        this.init(descriptor, JmsType.TOPIC);
    }

    public final Destination createDestination(String destName, JmsType jmsType) {
        return (Destination)(JmsType.TOPIC.equals(jmsType)?this.createTopic(destName):this.createQueue(destName));
    }

    public final Queue createQueue(String name) {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            try {
                return (Queue)this.ctx.lookup(name);
            } catch (NamingException var3) {
                throw new IntegrationRuntimeException("Error creating queue \'" + name + "\'!", var3);
            }
        }
    }

    public final Topic createTopic(String name) {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            try {
                return (Topic)this.ctx.lookup(name);
            } catch (NamingException var3) {
                throw new IntegrationRuntimeException("Error creating topic \'" + name + "\'!", var3);
            }
        }
    }

    public final MessageConsumer createConsumer(Destination destination) {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            try {
                return this.jmsSession.createConsumer(destination);
            } catch (JMSException var3) {
                throw new IntegrationRuntimeException("Error creating message consumer for destination \'" + destination + "\'!", var3);
            }
        }
    }

    public final MessageProducer createProducer(Destination destination) {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            try {
                return this.jmsSession.createProducer(destination);
            } catch (JMSException var3) {
                throw new IntegrationRuntimeException("Error creating message producer for destination \'" + destination + "\'!", var3);
            }
        }
    }

    public final TextMessage createTextMessage() {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            try {
                return this.jmsSession.createTextMessage();
            } catch (JMSException var2) {
                throw new IntegrationRuntimeException("Error creating text message!", var2);
            }
        }
    }

    public final void start() {
        if(this.jmsConnection != null) {
            try {
                LOG.info("starting JMS connection...");
                this.jmsConnection.start();
            } catch (JMSException var2) {
                throw new IntegrationRuntimeException("Error starting JMS!", var2);
            }
        }

    }

    public final void stop() {
        if(this.jmsConnection != null) {
            try {
                LOG.info("stopping JMS connection...");
                this.jmsConnection.stop();
            } catch (JMSException var2) {
                throw new IntegrationRuntimeException("Error stopping JMS!", var2);
            }
        }

    }

    public final void close() {
        if(this.jmsSession != null) {
            try {
                LOG.info("closing JMS connection...");
                this.jmsSession.close();
                this.jmsSession = null;
                this.jmsConnection.close();
                this.jmsConnection = null;
            } catch (JMSException var2) {
                throw new IntegrationRuntimeException("Error closing JMS!", var2);
            }
        }

    }

    public final Connection getJmsConnection() {
        if(this.jmsConnection == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            return this.jmsConnection;
        }
    }

    public final Session getJmsSession() {
        if(this.jmsSession == null) {
            throw new IntegrationRuntimeException("JMS is not initialized!");
        } else {
            return this.jmsSession;
        }
    }

    public final void purgeJms(String destinationName, JmsType jmsType) throws JMSException {
        this.getMessageCountAndCleanUp(destinationName, jmsType);
    }

    public final int getMessageCountAndCleanUp(String destinationName, JmsType jmsType) throws JMSException {
        int returnValue = 0;
        MessageConsumer consumer = null;

        try {
            consumer = this.createConsumer((Destination)(JmsType.TOPIC.equals(jmsType)?this.createTopic(destinationName):this.createQueue(destinationName)));

            for(Message msg = consumer.receiveNoWait(); msg != null; msg = consumer.receiveNoWait()) {
                ++returnValue;
                msg.acknowledge();
            }
        } finally {
            if(consumer != null) {
                consumer.close();
                consumer = null;
            }

        }

        return returnValue;
    }
}
