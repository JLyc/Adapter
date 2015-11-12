package telecom.core.wrapers.rv;

import com.cgi.eai.adapter.custom.telecom.config.TibrvDescriptor;
import com.tibco.tibrv.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.config.Configuration;
import telecom.core.AdapterCore;
import telecom.core.CommunicationClientInterface;
import telecom.core.wrapers.CommunicationInterface;
import telecom.core.wrapers.CommunicationMessageInterface;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public class RvCommunication implements CommunicationInterface, TibrvMsgCallback {
    private static final Log LOG = LogFactory.getLog(RvCommunication.class);

    // Configuration instance
    private static Configuration cfg = Configuration.getInstance();

    // RV fields
    private TibrvRvdTransport rvdTransport = null;
    private TibrvQueue rvQueue = null;
    private TibrvDescriptor tibRvDescriptor = cfg.getRvDescriptor();
    private TibrvListener tibrvListener = null;
    private boolean isListening = true;
    private CommunicationClientInterface communicationClient;

    public RvCommunication(CommunicationClientInterface communicationClient) {
        this.communicationClient = communicationClient;
    }

    @Override
    public void init() {
        LOG.info("Initializing RV connection...");
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            rvdTransport = new TibrvRvdTransport(tibRvDescriptor.getService(),
                    tibRvDescriptor.getNetwork(), tibRvDescriptor.getDaemon());
            if (rvdTransport == null) {
                LOG.error("RV transport is not initialized!");
                AdapterCore.shutDown(173);
            }
            rvQueue = new TibrvQueue();
            tibrvListener = new TibrvListener(rvQueue, this, rvdTransport, cfg.getSubjectDescriptor().getSubject(),null);
            LOG.info("RV Listening on subject: " + cfg.getSubjectDescriptor().getSubject());
        } catch (TibrvException e) {
            LOG.error("RV failed to initialize", e);
            AdapterCore.shutDown(173);
        }
    }

    @Override
    public void close() {
        try {
            tibrvListener.destroy();
            rvdTransport.destroy();
            Tibrv.close();
        } catch (TibrvException e) {
            LOG.error("Could not close connection!", e);
        }
    }

    @Override
    public void stopListening() {
        isListening=false;
    }

    @Override
    public void sendReply(CommunicationMessageInterface responseMsg, CommunicationMessageInterface sourceMsg) {
        try {
            if (responseMsg instanceof RVMessageInterfaceWraper) {
                rvdTransport.sendReply(((RVMessageInterfaceWraper) responseMsg).getMessage(), ((RVMessageInterfaceWraper) sourceMsg).getMessage());
            }
        } catch (TibrvException e) {
            LOG.error("Failed to send response" + responseMsg.getText(), e);
        }
    }

    @Override
    public void run() {
        while(isListening){
            try {
                rvQueue.dispatch();
//                            timed dispatch should make time to listen ?
//                            rvQueue.timedDispatch();
            } catch (TibrvException | InterruptedException e) {
                LOG.warn("Exception at rv listening thread fail to dispatch",e);
            }
        }
        LOG.info("RV listener stopped");
    }

    @Override
    public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
        communicationClient.request(new RVMessageInterfaceWraper(tibrvMsg));
    }
}
