package telecom.core;

import org.apache.commons.logging.*;
import telecom.config.*;
import telecom.core.wrapers.*;
import telecom.core.wrapers.jms.*;
import telecom.core.wrapers.rv.*;
import telecom.monitoring.*;
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

    /**
     * Return singleton instance of CommunicationClient
     * @return
     */
    public static CommunicationClient getInstance() {
        if (communicationClient == null)
        	communicationClient = new CommunicationClient();
        return communicationClient;
    }

    private CommunicationClient() {
            init();
    }

    /**
     * Initialize all enabled communication ways in config file e.g. JMS, RV, HawkRv
     */
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
        LOG.info("Executor closing with: "+executor.getQueue().size()+" tasks at queues and "  +executor.getActiveCount()+" active tasks: ");
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
        LOG.debug("Executing Request:\n "+msg.getText());
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
        LOG.debug("Executing Response:\n "+responseMsg.getText());
            if (responseMsg instanceof RVMessageInterfaceWraper) {
                rvListener.sendReply(responseMsg, sourceMsg);
            }
            if (responseMsg instanceof JmsMessageInterfaceWraper) {
                jmsListener.sendReply(responseMsg, sourceMsg);
            }
    }

    /**
     * Start listening for messages on initialized Listeners
     */
    public void startListening(){
        if(rvListener !=null) {
            Thread _rvListener = new Thread(rvListener);
            _rvListener.start();
            LOG.debug("is Rv listening -> " + _rvListener.isAlive());
        }
        if(jmsListener != null){
            Thread _jmsListener = new Thread(jmsListener);
            _jmsListener.start();
            LOG.debug("is Jms listening  -> " + _jmsListener.isAlive());
        }
        if(hawkMonitor != null){
            Thread _hawkMonitor = new Thread(hawkMonitor);
            _hawkMonitor.start();
            LOG.debug("is HawkRv monitoring -> " + _hawkMonitor.isAlive());
        }
        isListening = true;
    }

    /**
     * Stop listening for messages on initialized Listeners
     */
    public void stopListening(){
        isListening = false;
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
    }

    /**
     * Is listening thread active
     */
    public static boolean isListening() {
        return isListening;
    }

    /**
     * Return thread pool
     * @return
     */
    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

}
