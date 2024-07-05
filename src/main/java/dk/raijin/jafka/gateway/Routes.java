package dk.raijin.jafka.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Routes {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("r1", r -> r.path("/baeldung")
                        .uri("http://baeldung.com"))
                .route("r2", r -> r.path("/myOtherRouting")
                        .filters(f -> f.prefixPath("/myPrefix"))
                        .uri("http://othersite.com"))
                .build();
    }
}