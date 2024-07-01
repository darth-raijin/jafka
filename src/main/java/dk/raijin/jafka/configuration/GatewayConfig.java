package dk.raijin.jafka.configuration;

import dk.raijin.jafka.filter.PublishingFilter;
import dk.raijin.jafka.services.MessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public PublishingFilter publishingFilter(MessageService messageService) {
        return new PublishingFilter(messageService);
    }
}