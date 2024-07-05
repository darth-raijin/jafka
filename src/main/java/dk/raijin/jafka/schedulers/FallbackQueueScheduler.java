package dk.raijin.jafka.schedulers;

import dk.raijin.jafka.services.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FallbackQueueScheduler {

    private final KafkaService kafkaService;

    @Autowired
    public FallbackQueueScheduler(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Scheduled(fixedRate = 60000) // Retry every 60 seconds
    public void retryMessages() {
        kafkaService.retryFailedMessages();
    }
}