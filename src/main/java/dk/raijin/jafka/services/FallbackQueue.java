package dk.raijin.jafka.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * FallbackQueue is a simple in-memory queue that stores messages that could not be sent to Kafka.
 * The queue is used to retry sending the messages at a later time.
 * The queue has a maximum size, and if the queue is full, the oldest message is removed - taking a sliding window approach.
 */
@Service
public class FallbackQueue {

    static int maxMessages = 10000;

    public static class Message {
        private final String topic;
        private final byte[] message;

        public Message(String topic, byte[] message) {
            this.topic = topic;
            this.message = message;
        }

        public String getTopic() {
            return topic;
        }

        public byte[] getMessage() {
            return message;
        }
    }

    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>(maxMessages);


    /**
     * addMessage adds a message to the queue. If the queue is full, the oldest message is removed.
     */
    public void addMessage(String topic, byte[] message) {
        if (queue.size() == maxMessages) {
            queue.poll();
        }
        queue.add(new Message(topic, message));
    }

    public Message pollMessage() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
