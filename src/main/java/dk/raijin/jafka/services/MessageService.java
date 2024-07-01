package dk.raijin.jafka.services;


public interface MessageService {
    void sendMessage(String topic, String message);
}