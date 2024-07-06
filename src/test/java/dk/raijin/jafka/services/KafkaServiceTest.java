package dk.raijin.jafka.services;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.*;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class KafkaServiceTest {

    @Autowired
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private FallbackQueue fallbackQueue;

    @BeforeEach
    public void setUp() {
        fallbackQueue = new FallbackQueue();
    }

    @Test
    public void shouldConsumePublishedMessage_WhenServicePublishes() {
        String topic = UUID.randomUUID().toString();
        String message = "Some message";

        kafkaService.sendMessage(topic, message.getBytes());

        Consumer<byte[], byte[]> consumer = createConsumer(topic);

        ConsumerRecords<byte[], byte[]> records = KafkaTestUtils.getRecords(consumer);
        assertThat(records.count()).isGreaterThan(0);
        assertThat(new String(records.iterator().next().value())).isEqualTo(message);
    }

    @Test
    public void shouldAddMessageToFallbackQueue_whenKafkaIsDown() {
        String topic = UUID.randomUUID().toString();
        String message = "Some message";

        // Create a KafkaTemplate with an invalid broker to simulate Kafka being down
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "invalid:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        ProducerFactory<byte[], byte[]> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        KafkaTemplate<byte[], byte[]> invalidKafkaTemplate = new KafkaTemplate<>(producerFactory);

        KafkaService localKafkaService = new KafkaService(invalidKafkaTemplate, fallbackQueue);

        localKafkaService.sendMessage(topic, message.getBytes());

        assertThat(fallbackQueue.isEmpty()).isFalse();
        assertThat(new String(fallbackQueue.pollMessage().getMessage())).isEqualTo(message);
    }

    private Consumer<byte[], byte[]> createConsumer(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        ConsumerFactory<byte[], byte[]> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<byte[], byte[]> consumer = cf.createConsumer();
        consumer.subscribe(List.of(topic));
        return consumer;
    }
}
