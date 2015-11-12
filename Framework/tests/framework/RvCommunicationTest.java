package framework;

import org.junit.Before;
import org.junit.Test;
import telecom.config.Configuration;
import telecom.core.wrapers.rv.RvCommunication;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public class RvCommunicationTest {

    @Before
    public void preparation() {
        Configuration.getInstance().loadConfig(Paths.get("AdapterPropConfig.xml"));
    }

    @Test
    public void jmsTest() {
        RvCommunication rv= new RvCommunication(null);
        rv.init();
        Thread rvListening = new Thread(rv);
        rvListening.start();
        assertTrue("rv is not listening", rvListening.isAlive());
        rv.stopListening();
        rv.close();
        assertTrue("rv is still listening", rvListening.isAlive());
    }

}
