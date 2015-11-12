package telecom.core.wrapers.rv;

import com.logica.eai.test.bw.IntegrationRuntimeException;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvXml;
import telecom.core.wrapers.CommunicationMessageInterface;

import java.io.UnsupportedEncodingException;

public class RVMessageInterfaceWraper implements CommunicationMessageInterface {
	private TibrvMsg message = null;
	
	public RVMessageInterfaceWraper(TibrvMsg msg) {
		message = msg;
    }

    public RVMessageInterfaceWraper(String msg) throws TibrvException {
        createMessage(msg);
    }
    
    public String getText()
    {
        try
        {
            return new String(((TibrvXml) message.get("xml")).getBytes(),"UTF-8");
        }
        catch (TibrvException tibRvExc)
        {
            throw new IntegrationRuntimeException("Error getting text from message!", tibRvExc);
        }
        catch (UnsupportedEncodingException ueExc)
        {
            throw new IntegrationRuntimeException(ueExc);
        }
    }
    
    public void createMessage (final String strMessage) throws TibrvException
    {
        if (strMessage == null)
        {
            throw new IllegalArgumentException("Input message can not be null!");
        }
        message = new TibrvMsg();
        try
        {
            message.add("xml",new TibrvXml(strMessage.trim().getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException ueExc)
        {
            throw new IntegrationRuntimeException(ueExc);
        }
    }
    
    /**
     * Retrieves wrapped RV Message.
     *
     * @return TIBCO RV message
     */
    public final TibrvMsg getMessage()
    {
        return message;
    }
    
}