package telecom.core.wrapers;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public interface CommunicationInterface extends Runnable{

    void init();
    void close();
    void stopListening();
    void sendReply(CommunicationMessageInterface responseMsg, CommunicationMessageInterface sourceMsg);
}
