package telecom.hawk;

import COM.TIBCO.hawk.ami.*;
import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.config.Configuration;

/**
 * Created by JLyc on 30. 3. 2015.
 */
public class HawkMonitor implements Runnable {
    private static final Log LOG = LogFactory.getLog(HawkMonitor.class);
    private AmiSession _session;
    private TibrvQueue _queue;
    private boolean isListening = true;

    class methodGetStatus extends AmiMethod {

        public methodGetStatus() {
            super("get status", "return", AmiConstants.METHOD_TYPE_INFO);
        }

        @Override
        public AmiParameterList getArguments() {

            return null;
        }

        @Override
        public AmiParameterList getReturns() {
            AmiParameterList parameterList = new AmiParameterList();
            parameterList.add(new AmiParameter("Name", "Help", ""));

            return parameterList;
        }

        @Override
        public AmiParameterList onInvoke(AmiParameterList amiParameterList) throws Exception {
        	AmiParameterList parameterList = new AmiParameterList();
            parameterList.add(new AmiParameter("Name", "Help", "value"));
            parameterList.add(new AmiParameter("Namenext", "Help1", "14"));

            return parameterList;
        }
    }

    class methodKill extends AmiMethod {

        public methodKill() {
            super("Kill", "Adapter instance is killed and resources released. Use with caution. A message can be processed without response being sent to JMS.", AmiConstants.METHOD_TYPE_ACTION);
        }

        public AmiParameterList getArguments() {
            return null;
        }

        public AmiParameterList getReturns() {
            return null;
        }

        public AmiParameterList onInvoke(AmiParameterList arg0) throws Exception {
//            String result = _manager.kill();
//            if ( !result.equals("Success") ) {
//                throw new AmiException(AmiErrors.AMI_REPLY_ERR, result);
//            }

            return null;
        }
    }



    public HawkMonitor() {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            _queue = new TibrvQueue();

            Configuration cfg = Configuration.getInstance();

            String hawk_service = cfg.getTibhawkrvDescriptor().getService();
            String hawk_daemon = cfg.getTibhawkrvDescriptor().getDaemon();
            String hawk_network = cfg.getTibhawkrvDescriptor().getNetwork();


            _session = new AmiSession(hawk_service, hawk_network, hawk_daemon, _queue,
                    "com.cgi.eai.adapter.custom.telecom",
                    Configuration.getInstance().getTibhawkrvDescriptor().getMicroAgentName(), "Custom adapter instance", null);

            _session.addMethod(new methodGetStatus());
            _session.addMethods(_session);
            _session.createCommonMethods("Synch (Custom Adapter Application)",
                    "1.0.0",
                    "2015-03-30",
                    1,
                    0,
                    0);
            _session.announce();
            LOG.info("Hawk monitor successfully initialized.");

        } catch (AmiException | TibrvException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopListening() {
        isListening = false;
    }

    @Override
    public void run() {
        while (isListening) {
            try {
                _queue.dispatch();
//                            rvQueue.timedDispatch();
            } catch (TibrvException | InterruptedException e) {
                LOG.warn("Exception at hawk monitor thread fail to dispatch", e);
            }
        }
        LOG.info("Hawk monitor stopped");
    }

    public void close() {
        try {
            _session.stop();
            LOG.info("Ami session successfully closed.");
        } catch (Exception ex) {
            _session = null;
            LOG.info("Ami session closed.");
        }
    }

}
