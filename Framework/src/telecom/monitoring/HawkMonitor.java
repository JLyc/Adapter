package telecom.monitoring;

import COM.TIBCO.hawk.ami.*;
import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import telecom.config.Configuration;
import telecom.core.AdapterCore;
import telecom.statistic.AdapterStatistic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by JLyc on 30. 3. 2015.
 */
public class HawkMonitor implements Runnable {
    private static final Log LOG = LogFactory.getLog(HawkMonitor.class);
    private AmiSession _session;
    private TibrvQueue _queue;
    private boolean isListening = true;

    private Map<String, String> adapterStatistic;

    /**
     * Adapter statistic method.
     * Sending statistic to hawk ami
     */
    class methodGetStatus extends AmiMethod {

        public methodGetStatus() {
            super("getAdapterStatus", "Return statistic of adapter", AmiConstants.METHOD_TYPE_INFO);
            adapterStatistic = new HashMap<>(AdapterStatistic.getStatistic());
            for(Map.Entry<String, String> statisticName : adapterStatistic.entrySet()){
                setIndexName(statisticName.getKey());
            }
        }

        @Override
        public AmiParameterList getArguments() {
            return null;
        }

        @Override
        public AmiParameterList getReturns() {
            AmiParameterList parameterList = new AmiParameterList();
            adapterStatistic = new HashMap<>(AdapterStatistic.getStatistic());
            for(Map.Entry<String, String> statisticName : adapterStatistic.entrySet()){
                parameterList.add(new AmiParameter(statisticName.getKey(), statisticName.getValue()));
            }
            return parameterList;
        }

        @Override
        public AmiParameterList onInvoke(AmiParameterList amiParameterList) throws Exception {
            adapterStatistic = new HashMap<>(AdapterStatistic.getStatistic());
            AmiParameterList parameterList = new AmiParameterList();
            for (Map.Entry<String, String> statisticName : adapterStatistic.entrySet()) {
                parameterList.add(new AmiParameter(statisticName.getKey(), statisticName.getValue()));
            }
            return parameterList;
        }
    }

    /**
     * Adapter shutdown method.
     * Sending shutdown request via hawk ami
     */
    class methodKill extends AmiMethod {

        public methodKill() {
            super("shutDownAdapter", "Adapter instance is killed and resources released. Use with caution.", AmiConstants.METHOD_TYPE_ACTION);
        }

        public AmiParameterList getArguments() {
            return null;
        }

        public AmiParameterList getReturns() {
            return null;
        }

        public AmiParameterList onInvoke(AmiParameterList arg0) throws Exception {
            AdapterCore.shutDown(1799);
            return null;
        }
    }

    /**
     * Adapter setter method.
     * Setting adapter logging level via hawk ami
     */
    class methodChangeLogLevel extends AmiMethod {

        public methodChangeLogLevel() {
            super("changeLogLevel", "Change log level for adapter loging", AmiConstants.METHOD_TYPE_ACTION);
        }

        @Override
        public AmiParameterList getArguments() {
            AmiParameterList parameterList = new AmiParameterList();
            String[] legalChoises = {LogLvl.DEBUG.getName(), LogLvl.ERROR.getName(), LogLvl.FATAL.getName(), LogLvl.INFO.getName(), LogLvl.WARN.getName(), LogLvl.OFF.getName()};
            AmiParameter logLvl = new AmiParameter("log_level", "Level of adapter logging.", LogLvl.DEBUG.getName());
            /* Needed in case of tibco bug "personal remark andrej.socha@cgi.com"
            int[] legalChoisesNumber = {0};
            AmiParameter logLvlNumber = new AmiParameter("___", "place_holder", 0);
            logLvlNumber.setValueChoices(legalChoisesNumber);
            logLvlNumber.setLegalChoices(legalChoisesNumber);
            parameterList.add(logLvlNumber); */
            logLvl.setValueChoices(legalChoises);
            logLvl.setLegalChoices(legalChoises);
            parameterList.add(logLvl);
            return parameterList;
        }

        @Override
        public AmiParameterList getReturns() {
            return null;
        }

        @Override
        public AmiParameterList onInvoke(AmiParameterList amiParameterList) throws Exception {
            String _value = (String) amiParameterList.getParameter(0).getValue();
            Level loglvl = AdapterCore.getLoglvl();
            if (_value.equalsIgnoreCase(LogLvl.INFO.getName())) loglvl = Level.INFO;
            else if (_value.equalsIgnoreCase(LogLvl.WARN.getName())) loglvl = Level.WARN;
            else if (_value.equalsIgnoreCase(LogLvl.DEBUG.getName())) loglvl = Level.DEBUG;
            else if (_value.equalsIgnoreCase(LogLvl.ERROR.getName())) loglvl = Level.ERROR;
            else if (_value.equalsIgnoreCase(LogLvl.FATAL.getName())) loglvl = Level.FATAL;
            else if (_value.equalsIgnoreCase(LogLvl.OFF.getName())) loglvl = Level.OFF;
            else LOG.info("log level not changed:");
            LOG.info("log level set: " + loglvl);
            AdapterCore.setLoglvl(loglvl);
            LogManager.getRootLogger().setLevel(loglvl);
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
            _session.addMethod(new methodKill());
            _session.addMethod(new methodChangeLogLevel());
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
                LOG.warn("Exception at monitoring monitor thread fail to dispatch", e);
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
