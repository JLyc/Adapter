package framework;

import org.junit.Test;
import telecom.config.Configuration;
import telecom.core.MessageLifeCycle;
import telecom.core.wrapers.CommunicationMessageInterface;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by JLyc on 1. 4. 2015.
 */
public class MessageThreadTest {

    private static boolean isRv = true;
    private static boolean isJms = true;


    private String msg = "<con:snmp-request-message xmlns:con=\"http://www.cgi.com/eai/adapter/custom/telecom/config\">\n" +
            "  <con:system>WeatherProbes</con:system>\n" +
            "  <con:snmp-version>0</con:snmp-version>\n" +
            "  <con:ip-address>127.0.0.1</con:ip-address>\n" +
            "  <con:port>161</con:port>\n" +
            "  <con:community>public</con:community>\n" +
            "  <con:request-type>SET</con:request-type>\n" +
            "  <con:oid-value>1.3.6.1.2.148</con:oid-value>\n" +
//              "  <con:oid-value>1.3.6.1.4.1.27070.3.4.1.2.</con:oid-value>\n" +
            "  <con:retries-count>2</con:retries-count>\n" +
            "  <con:timeout>100</con:timeout>\n" +
            "</con:snmp-request-message>";

    @Test
    public void MessageThreadTest() throws InterruptedException {
        Configuration.getInstance().loadConfig(Paths.get("AdapterPropConfig.xml"));
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        executor.execute(new MessageLifeCycle(new JmsRvMessageInterface(msg)));
        executor.execute(new MessageLifeCycle(new JmsRvMessageInterface(msg)));
        executor.execute(new MessageLifeCycle(new JmsRvMessageInterface(msg)));
        TimeUnit.SECONDS.sleep(10);
        System.out.println("ended");
/*        try {
            if (isRv) {
                executor.execute(new MessageLifeCycle(new RVMessageInterfaceWraper(msg)));
            }
            if (isJms) {
                executor.execute(new MessageLifeCycle(new JmsMessageInterfaceWraper(msg)));
            }
        } catch (TibrvException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        */
    }


    public class JmsRvMessageInterface implements CommunicationMessageInterface {
        private String msg;

        public JmsRvMessageInterface(String msg) {
            this.msg = msg;
        }

        @Override
        public String getText() {
            return msg;
        }
    }
}
