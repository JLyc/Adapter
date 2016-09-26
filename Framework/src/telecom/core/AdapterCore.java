package telecom.core;

import com.cgi.eai.adapter.custom.telecom.config.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;
import org.apache.log4j.net.SyslogAppender;
import telecom.config.*;
import telecom.monitoring.*;

import java.io.File;
import java.lang.reflect.Field;
import java.math.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author andrej.socha@cgi.com
 *         <p>
 *         Main class for Custom Adapter
 */
public class AdapterCore {
    private static final Log LOG = LogFactory.getLog(AdapterCore.class);

    private static Level loglvl = Level.DEBUG;
    private static Path dConfig = Paths.get("AdapterPropConfig.xml");
    private static CommunicationClient communicationClient;

    public static void main(String[] args) {
        resolveArgs(args);
        if(Configuration.getInstance().getNativeLibDefinition()!=null)
            setNativeLib();
        init();
    }

    /**
     * Set native lib to be used mainly for *.so and *.dll native libs for RV communication
     */
    private static void setNativeLib() {
        System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + Configuration.getInstance().getNativeLibDefinition());
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Unable load native library", e);
        }
        LOG.debug("Using java.library.path -> "+ System.getProperty("java.library.path"));
    }

    /**
     * resolveArgs handle start up input parameters and save them to configuration properties
     *
     * @param args java main input parameters
     */
    private static void resolveArgs(String[] args) {
        List<String> params = Arrays.asList(args);
        Configuration cfg = Configuration.getInstance();

        LOG.debug("Resolving given parameters");
        if (params.size() == 1 && params.get(0).equalsIgnoreCase("?")) {
            usage();
            shutDown(1700);
        }

        if (params.size() % 2 != 0) {
            LOG.warn("Illegal params see \"java -jar \'AdapterName\'.jar ? \" for usage \n You used [" + params + "}\nAdapter ignore your prams and use default");
            usage();
            cfg.loadConfig(dConfig);
            return;
        }

        if (params.contains("-loglvl")) {
            String _value = params.get(params.indexOf("-loglvl") + 1);
            if (_value.equalsIgnoreCase(LogLvl.INFO.getName())) loglvl = Level.INFO;
            if (_value.equalsIgnoreCase(LogLvl.WARN.getName())) loglvl = Level.WARN;
            if (_value.equalsIgnoreCase(LogLvl.DEBUG.getName())) loglvl = Level.DEBUG;
            if (_value.equalsIgnoreCase(LogLvl.ERROR.getName())) loglvl = Level.ERROR;
            if (_value.equalsIgnoreCase(LogLvl.FATAL.getName())) loglvl = Level.FATAL;
            if (_value.equalsIgnoreCase(LogLvl.OFF.getName())) loglvl = Level.OFF;
            LOG.info("log level set: "+loglvl);
        }
        LogManager.getRootLogger().setLevel(loglvl);


        if (params.contains("-config")) {
            String _value = params.get(params.indexOf("-config") + 1);
            dConfig = Paths.get(_value);
        }
        cfg.loadConfig(dConfig);


        if (params.contains("-msgType")) {
            String _value = params.get(params.indexOf("-msgType") + 1);
            if (_value.toLowerCase().contains("jms")) cfg.getCommunicationType().setJms(true);
            else cfg.getCommunicationType().setJms(false);
            if (_value.toLowerCase().contains("rv")) cfg.getCommunicationType().setRv(true);
            else cfg.getCommunicationType().setRv(false);
            LOG.info("Msg type - jms: "+cfg.getCommunicationType().isJms()+" - rv: "+cfg.getCommunicationType().isRv());
        }

        if (params.contains("-threads")) {
            String _value = params.get(params.indexOf("-threads") + 1);
            cfg.getThreadsDescriptor().setThreadsNo(BigInteger.valueOf(Integer.parseInt(_value)));
            LOG.info("Threads set to: "+ _value);
        }
        if (params.contains("-subject")) {
            String _value = params.get(params.indexOf("-subject") + 1);
            cfg.getSubjectDescriptor().setSubject(_value);
            LOG.info("Subject set to: "+ _value);
        }
        if (params.contains("-jmsSubject")) {
            String _value = params.get(params.indexOf("-jmsSubject") + 1);
            cfg.getJmsResponseSubject().setResponseSubject(_value);
            LOG.info("Jms subject set to: " + _value);
        }
    }

    /**
     * Init register shutdown hook and create JMS/RV and start listening
     */
    private static void init() {
        Runtime.getRuntime().addShutdownHook(new ShutDown());
        communicationClient = CommunicationClient.getInstance();
        LOG.debug("Starting listeners...");
        communicationClient.startListening();
        LOG.info("Adapter is Ready and listening");
    }

    /**
     * System.exit values
     * 1700 - Graceful adapter shutdown.
     * 1701 - Adapter answer request for usage message and close himself.
     * 1702 - Problem with configuration file binding, adapter unable to run without configuration properties.
     * 1703 - Problem with RV communication. Adapter closed no communication available.
     * 1704 - Problem with JMS communication. Adapter closed no communication available.
     */
    public static void shutDown(int code) {
        LOG.warn("Adapter closing with code: {" + code + "}. See documentation for more info.");
        System.exit(code);
    }

    /**
     * ShutDown hook class for closing adapter
     */
    private static class ShutDown extends Thread {

        @Override
        public void run() {
            super.run();
            LOG.info("Adapter shutting down...");
            if (CommunicationClient.isListening()) {
                LOG.debug("Finishing listening thread");
                communicationClient.stopListening();
                LOG.debug("Closing executor");
                communicationClient.shutDownExecutor();
            }
            LOG.info("Wait for request to be completed ...");
            LOG.debug("Adapter Closed");
        }
    }

    /**
     * Setter for changing log level
     * @param loglvl
     */
    public static void setLoglvl(Level loglvl) {
        AdapterCore.loglvl = loglvl;
    }

    /**
     * Getter for geting actual log level
     * @return loglvl value of logging
     */
    public static Level getLoglvl() {
        return loglvl;
    }

    /**
     * Terminal output (manual like) message
     */
    private static void usage() {
        System.out.print("\nUsage:");
        System.out.println("  \"java -jar CustomAdapter.jar [-loglvl lvl][-config config] [-msgType msgType] [-Threads threads] [-subject subject] [-jmsResponse subject]\". \n");
        System.out.println(" -config config         Optional argument specifying location of external config");
        System.out.println("            	        file. Replace \"config\" with file path to config file.");
        System.out.println("            	        Default is to use config packaged with jar file.\n");
        System.out.println(" -loglvl lvl            Optional argument specifying log level for logger.");
        System.out.println("            	        Replace \"lvl\" with \'info\',\'warn\',\'debug\',\'error\',\'fatal\' or \'off\'.\n");
        System.out.println("            	        Default is level \'crit\' showing only critical errors.\n");
        System.out.println(" -msgType msgType       Optional argument specifying what is used to receive request and send replay");
        System.out.println("            	        Replace \"msgType\" with \'jmsListener\',\'rvListener\' or \'jmsrv\'.\n");
        System.out.println("            	        Default is rvListener stands fro Rendezvous.\n");
        System.out.println(" -threads threads       Optional argument specifying how many threads can be used to handle requests");
        System.out.println("            	        Replace \"threads\" with number e.g. \'100\' or \'8473\'.");
        System.out.println("            	        \"0\" is crating threads dynamically as needed\n");
        System.out.println("            	        Default is \"0\" creating as many threads as needed.\n");
        System.out.println(" -subject subject       Optional argument defining on what subject are message type listening other then default");
        System.out.println("            	        Replace \"subject\" with subject e.g. \'JMS.CUSTOM_ADAPTER.QUEUE\'.\n");
        System.out.println("            	        By default listeners listen on subject defined at config file.\n");
        System.out.println(" -jmsResponse subject   Optional argument defining on what subject is jms message response other then default\n");
        System.out.println("            	        Replace \"subject\" with response subject e.g. \'JMS.CUSTOM_ADAPTER.QUEUE\'.\n");
        System.out.println("            	        By default listeners listen on subject defined at config file.\n");

    }
}
