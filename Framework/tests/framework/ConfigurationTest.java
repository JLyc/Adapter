package framework;

import com.cgi.eai.adapter.custom.telecom.config.CustomAdapterConfig;
import org.junit.Test;
import telecom.config.Configuration;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Created by JLyc on 4. 4. 2015.
 */
public class ConfigurationTest {

    @Test
    public void loadConfigTest() {
        Path falsePath = FileSystems.getDefault().getPath("AdapterPropConfig.xml");
        Configuration config = Configuration.getInstance();
        CustomAdapterConfig cfg = config.loadConfig(falsePath);
        assertNotNull(cfg.getTibhawkrvDescriptor());
        assertNotNull(cfg.getJmsDescriptor());
        assertNotNull(cfg.getSubjectDescriptor());
        assertNotNull(cfg.getThreadsDescriptor());
        assertNotNull(cfg.getTibrvDescriptor());
        assertNotNull(cfg.getPluginDefProp());
        assertNotNull(cfg.getCommunicationType());
        assertFalse(config.isJms());
        assertTrue(config.isRv());
    }
}
