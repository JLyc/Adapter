package telecom.core.wrapers;

import telecom.core.wrapers.jms.JmsMessageInterfaceWraper;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public interface CommunicationInterface extends Runnable{

    void init();
    void close();
    void stopListening();
    void sendReply(CommunicationMessageInterface responseMsg, CommunicationMessageInterface sourceMsg);
    void send(JmsMessageInterfaceWraper replyMsg, String destination);
}
