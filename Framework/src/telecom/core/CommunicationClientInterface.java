package telecom.core;


import telecom.core.wrapers.CommunicationMessageInterface;

/**
 * Created by JLyc on 13. 4. 2015.
 */
public interface CommunicationClientInterface {
    public void request(CommunicationMessageInterface msg);
}
