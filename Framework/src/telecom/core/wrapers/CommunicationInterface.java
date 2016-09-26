package telecom.core.wrapers;

public interface CommunicationInterface extends Runnable{

    void init();
    void close();
    void stopListening();
    void sendReply(CommunicationMessageInterface responseMsg, CommunicationMessageInterface sourceMsg);
}
