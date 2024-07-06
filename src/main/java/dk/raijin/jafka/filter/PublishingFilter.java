package dk.raijin.jafka.filter;

import dk.raijin.jafka.protos.RequestProto;
import dk.raijin.jafka.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PublishingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(PublishingFilter.class);
    private final MessageService messageService;

    public PublishingFilter(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("PublishingFilter: custom global filter invoked");

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    RequestProto.Request message = RequestProto.Request.newBuilder()
                            .setIdempotencyKey(UUID.randomUUID().toString())
                            .setMethod(exchange.getRequest().getMethod().name())
                            .setPath(exchange.getRequest().getPath().value())
                            .setAuthToken(exchange.getRequest().getHeaders().getFirst("Authorization"))
                            .setClientIp(exchange.getRequest().getRemoteAddress().toString()) // TODO: Brug lige debugger igen
                            .setTimestamp(System.currentTimeMillis())
                            .build();

                    logger.info("PublishingFilter: Sending message to Kafka: {}", message);
                    messageService.sendMessage("Auth", message);
                }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}