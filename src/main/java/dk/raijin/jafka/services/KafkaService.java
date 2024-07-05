package dk.raijin.jafka.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService implements MessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FallbackQueue fallbackQueue;

    @Autowired
    public KafkaService(KafkaTemplate<String, String> kafkaTemplate, FallbackQueue fallbackQueue) {
        this.kafkaTemplate = kafkaTemplate;
        this.fallbackQueue = fallbackQueue;
    }

    @Override
    public void sendMessage(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message).get();
        } catch (Exception e) {
            fallbackQueue.addMessage(topic, message);
        }
    }

    public void retryFailedMessages() {
        while (!fallbackQueue.isEmpty()) {
            FallbackQueue.Message failedMessage = fallbackQueue.pollMessage();
            if (failedMessage != null) {
                sendMessage(failedMessage.getTopic(), failedMessage.getMessage());
            }
        }
    }
}