package telecom.core;


import telecom.core.wrapers.CommunicationMessageInterface;

public interface CommunicationClientInterface {
    void request(CommunicationMessageInterface msg);
}
