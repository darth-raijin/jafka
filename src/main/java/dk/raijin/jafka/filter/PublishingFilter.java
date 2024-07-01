package dk.raijin.jafka.filter;

import dk.raijin.jafka.services.MessageService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PublishingFilter implements GlobalFilter, Ordered {

    private final MessageService messageService;

    public PublishingFilter(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    String data = "Data to send";
                    messageService.sendMessage("topic", data);
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}