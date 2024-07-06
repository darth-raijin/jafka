package dk.raijin.jafka.services;

import com.google.protobuf.Message;

public interface MessageService {
    void sendMessage(String topic, Message message);
}