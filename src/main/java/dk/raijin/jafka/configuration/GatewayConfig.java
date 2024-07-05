package dk.raijin.jafka.configuration;

import dk.raijin.jafka.filter.PublishingFilter;
import dk.raijin.jafka.services.MessageService;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final MessageService messageService;

    public GatewayConfig(MessageService messageService) {
        this.messageService = messageService;
    }

    @Bean
    public GlobalFilter publishingFilter() {
        return new PublishingFilter(messageService);
    }
}