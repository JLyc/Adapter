package telecom.config;

import com.cgi.eai.adapter.custom.telecom.config.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telecom.core.AdapterCore;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by JLyc on 26. 3. 2015.
 * @author andrej.socha@cgi.com
 * 
 * Class for loading properties xml for custom adapter.
 */
public class Configuration {
    private static final Log LOG = LogFactory.getLog(Configuration.class);
    private static CustomAdapterConfig generatedConfig;

    private static Configuration configuration;

    public static Configuration getInstance() {
        if (configuration == null) configuration = new Configuration();
        return configuration;
    }

    /**
     * Method loadConfig will try to load configuration from jar file resources or external file
     * In case of binding error application is terminated.
     * ConfigFile.xml is bounded with XSD schema for generated classes @see CustomAdapterConfig
     *
     * @param configPath relative or absolute path to file in Path format
     *
     * @return CustomAdapterConfig instance of generated classes
     */
    public CustomAdapterConfig loadConfig(Path configPath){
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath.toString());
        if (in == null){
            try
            {
                LOG.debug("Loading custom \""+ configPath +"\"config");
                in = new FileInputStream(configPath.toFile());
            }
            catch (FileNotFoundException e)
            {
                LOG.error("File not found! Using default file." + configPath, e);
            }
        }
        try
        {
            JAXBContext jc = JAXBContext.newInstance(CustomAdapterConfig.class.getPackage().getName());
            generatedConfig = (CustomAdapterConfig) jc.createUnmarshaller().unmarshal(in);
            LOG.info("Configuration "+configPath+" was read");
        }
        catch (JAXBException jaxbExc)
        {
            LOG.error("Problem binding " + configPath + "!\n", jaxbExc);
            AdapterCore.shutDown(172);
        }
        return generatedConfig;
    }

    public boolean isRv(){
        return generatedConfig.getCommunicationType().isRv();
    }

    public boolean isJms(){
        return generatedConfig.getCommunicationType().isJms();
    }

    public boolean isHawkEnabled(){
        return generatedConfig.getTibhawkrvDescriptor().isMonitorEnabled();
    }

    public CommunicationType getCommunicationType(){
        return generatedConfig.getCommunicationType();
    }

    public String getNativeLibDefinition(){
        return generatedConfig.getNativeLibDefinition();
    }

    public TibrvDescriptor getRvDescriptor()
    {
        return generatedConfig.getTibrvDescriptor();
    }

    public JmsDescriptor getJmsDescriptor()
    {
        return generatedConfig.getJmsDescriptor();
    }

    public JmsResponseSubject getJmsResponseSubject()
    {
        return generatedConfig.getJmsResponseSubject();
    }

    public ThreadsDescriptor getThreadsDescriptor()
    {
        return generatedConfig.getThreadsDescriptor();
    }

    public SubjectDescriptor getSubjectDescriptor(){
        return generatedConfig.getSubjectDescriptor();
    }

    public TibhawkrvDescriptor getTibhawkrvDescriptor(){
        return generatedConfig.getTibhawkrvDescriptor();
    }

    public List<NameValue> getPluginDefProp(){
        return generatedConfig.getPluginDefProp().getProperty();
    }

}
