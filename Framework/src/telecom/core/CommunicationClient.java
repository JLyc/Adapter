package telecom.core;

import org.apache.commons.logging.*;
import telecom.config.*;
import telecom.core.wrapers.*;
import telecom.core.wrapers.jms.*;
import telecom.core.wrapers.rv.*;
import telecom.hawk.*;
import telecom.statistic.*;

import java.util.concurrent.*;

/**
 * Created by JLyc on 26. 3. 2015.
 * @author andrej.socha@cgi.com
 *
 * Adapter communication client for Tibco <-> Custom Adapter communication by RV or JMS
 */
public class CommunicationClient implements CommunicationClientInterface {
    private static final Log LOG = LogFactory.getLog(CommunicationClient.class);
    private static CommunicationClient communicationClient = null;

    private static boolean isListening = false;

    // Threads pool
    private static ThreadPoolExecutor executor = null;

    // Configuration instance
    private static Configuration cfg = Configuration.getInstance();
    CommunicationInterface rvListener;
    CommunicationInterface jmsListener;

    HawkMonitor hawkMonitor;
    public static CommunicationClient getInstance() {
        if (communicationClient == null)
        	communicationClient = new CommunicationClient();
        return communicationClient;
    }
    private CommunicationClient() {
            init();
    }

    private void init(){
        initExecutor();

        if (cfg.isRv()) {
            rvListener = new RvCommunication(this);
            rvListener.init();
        }
        if (cfg.isJms()) {
            jmsListener = new JmsCommunication(this);
            jmsListener.init();
        }
        if (cfg.isHawkEnabled()){
            hawkMonitor = new HawkMonitor();
        }
    }

    /**
     * Create thread pool depends on configuration properties "threadsNo"
     */
    private void initExecutor() {
        int _threadsNo = cfg.getThreadsDescriptor().getThreadsNo().intValue();
        if (_threadsNo>0) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(_threadsNo);
        }else{
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }
        LOG.debug("Initialization of thread pool complete.");
    }

    public void shutDownExecutor(){
        executor.shutdown();
        LOG.info("Executor closing with "+executor.getQueue().size()+"tasks at queues and "  +executor.getActiveCount()+" active tasks: ");
        //TODO change based on performance
        try {
            while(!(executor.getQueue().size()==0&&executor.getActiveCount()==00)){
                TimeUnit.SECONDS.sleep(1);
            }
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.info("Executor closed with: "+executor.getQueue().size()+" tasks at queues and "  +executor.getActiveCount()+" active tasks.");
        }
    }

    /**
     * Entry point for all Tibco messages to adapter.
     * Free thread is assign to message for handling
     *
     * @param msg Rv or Jms message
     */
    public synchronized void request(CommunicationMessageInterface msg){
        LOG.debug("Executing msg "+msg);
        AdapterStatistic.increaseValue("MsgCount", 1);
        executor.execute(new MessageLifeCycle(msg));
    }

    /**
     * Exit point for messages from adapter to tibco.
     * Message with response information
     *
     * @param sourceMsg request message Rv or Jms
     * @param responseMsg response message Rv or Jms
     */
    public synchronized void response(CommunicationMessageInterface sourceMsg, CommunicationMessageInterface responseMsg){
        LOG.debug("Response "+responseMsg.getText());
            if (responseMsg instanceof RVMessageInterfaceWraper) {
                rvListener.sendReply(responseMsg, sourceMsg);
            }
            if (responseMsg instanceof JmsMessageInterfaceWraper) {
//                Destination destination = (JmsMessageInterfaceWraper) sourceMsg.getMessage().getJMSReplyTo();
//                jmsListener.sendReply(responseMsg, sourceMsg);
                jmsListener.send((JmsMessageInterfaceWraper) responseMsg, Configuration.getInstance().getSubjectDescriptor().getSubject());
            }
    }

    /**
     * Start listening for messages on initialized Listeners
     */
    public void startListening(){
        if(rvListener !=null) {
            new Thread(rvListener).start();
        }
        if(jmsListener != null){
            new Thread(jmsListener).start();
        }
        if(hawkMonitor != null){
            new Thread(hawkMonitor).start();
        }
        LOG.info("Running listeners: RV="+rvListener+", JMS="+jmsListener+", HawkMonitor="+hawkMonitor);
        isListening = true;
    }

    /**
     * Stop listening for messages on initialized Listeners
     */
    public void stopListening(){
        if(rvListener !=null) {
            rvListener.stopListening();
            rvListener.close();
        }
        if(jmsListener != null){
            jmsListener.stopListening();
            jmsListener.close();
        }
        if(hawkMonitor != null){
            hawkMonitor.stopListening();
            hawkMonitor.close();
        }
        isListening = false;
    }

    /**
     * Is listening thread active
     */
    public static boolean isListening() {
        return isListening;
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

}
