package framework;

import org.junit.Before;
import org.junit.Test;
import telecom.config.Configuration;
import telecom.core.wrapers.jms.JmsCommunication;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public class JmsCommunicationTest {

	@Before
    public void preparation() {
        Configuration.getInstance().loadConfig(Paths.get("AdapterPropConfig.xml"));
    }

    @Test
    public void jmsTest() throws InterruptedException {
        JmsCommunication jms = new JmsCommunication(null);
        jms.init();
        Thread jmsListening = new Thread(jms);
        jmsListening.start();
        assertTrue("jms is not listening", jmsListening.isAlive());
        jms.stopListening();
        assertTrue("jms is still listening", jmsListening.isAlive());
        Thread.sleep(5000);
    }

}
