package dk.raijin.jafka.services;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService implements MessageService {

    private final KafkaTemplate<byte[], byte[]> kafkaTemplate;
    private final FallbackQueue fallbackQueue;

    @Autowired
    public KafkaService(KafkaTemplate<byte[], byte[]> kafkaTemplate,
                        FallbackQueue fallbackQueue) {
        this.kafkaTemplate = kafkaTemplate;
        this.fallbackQueue = fallbackQueue;
    }

    @Override
    public void sendMessage(String topic, Message message) {
        byte[] serializedMessage = message.toByteArray();

        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, serializedMessage);
            kafkaTemplate.send(record).get();
        } catch (Exception e) {
            fallbackQueue.addMessage(topic, serializedMessage);
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

    public void sendMessage(String topic, byte[] message) {
        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, message);
            kafkaTemplate.send(record).get();
        } catch (Exception e) {
            fallbackQueue.addMessage(topic, message);
        }
    }
}
