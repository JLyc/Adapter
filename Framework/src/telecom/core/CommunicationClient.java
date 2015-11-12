package telecom.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.config.Configuration;
import telecom.core.wrapers.CommunicationInterface;
import telecom.core.wrapers.CommunicationMessageInterface;
import telecom.core.wrapers.jms.JmsCommunication;
import telecom.core.wrapers.jms.JmsMessageInterfaceWraper;
import telecom.core.wrapers.rv.RVMessageInterfaceWraper;
import telecom.core.wrapers.rv.RvCommunication;
import telecom.hawk.HawkMonitor;
import telecom.statistic.AdapterStatistic;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by JLyc on 26. 3. 2015.
 * @author andrej.socha@cgi.com
 *
 * Adapter communication client for Tibco <-> Custom Adapter communication by RV or JMS
 */
public class CommunicationClient implements CommunicationClientInterface {
    private static final Log LOG = LogFactory.getLog(CommunicationClient.class);
    private static CommunicationClient communicationClient = null;
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
        }
        if (cfg.isJms()) {
            jmsListener = new JmsCommunication(this);
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
            LOG.info("Executor closed with "+executor.getQueue().size()+"tasks at queues and "  +executor.getActiveCount()+" Active tasks: ");
        }
    }

    /**
     * Entry point for all Tibco messages to adapter.
     * Free thread is assign to message for handling
     *
     * @param msg Rv or Jms message
     */
    public synchronized void request(CommunicationMessageInterface msg){
        executor.execute(new MessageLifeCycle(msg));
        AdapterStatistic.increaseValue("MsgCount", 1);
    }

    /**
     * Exit point for messages from adapter to tibco.
     * Message with response information
     *
     * @param sourceMsg request message Rv or Jms
     * @param responseMsg response message Rv or Jms
     */
    public synchronized void response(CommunicationMessageInterface sourceMsg, CommunicationMessageInterface responseMsg){
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
            new Thread(rvListener).start();
        }
        if(jmsListener != null){
            new Thread(jmsListener).start();
        }
        if(hawkMonitor != null){
            new Thread(hawkMonitor).start();
        }
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
    }

}
