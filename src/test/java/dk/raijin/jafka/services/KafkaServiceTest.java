package dk.raijin.jafka.services;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class KafkaServiceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

        kafkaService.sendMessage(topic, message);

        Consumer<String, String> consumer = createConsumer(topic);

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer);
        assertThat(records.count()).isGreaterThan(0);
        assertThat(records.iterator().next().value()).isEqualTo(message);
    }

    @Test
    public void shouldAddMessageToFallbackQueue_whenKafkaIsDown() {
        String topic = UUID.randomUUID().toString();
        String message = "Some message";

        // Create a KafkaTemplate with an invalid broker to simulate Kafka being down
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "invalid:29092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        KafkaTemplate<String, String> invalidKafkaTemplate = new KafkaTemplate<>(producerFactory);

        KafkaService localKafkaService = new KafkaService(invalidKafkaTemplate, fallbackQueue);

        localKafkaService.sendMessage(topic, message);

        assertThat(fallbackQueue.isEmpty()).isFalse();
        assertThat(fallbackQueue.pollMessage().getMessage()).isEqualTo(message);
    }

    private Consumer<String, String> createConsumer(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> consumer = cf.createConsumer();
        consumer.subscribe(List.of(topic));
        return consumer;
    }
}
