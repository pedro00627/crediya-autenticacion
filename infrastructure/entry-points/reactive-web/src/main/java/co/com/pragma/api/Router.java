package co.com.pragma.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Router {
    @Bean
    public RouterFunction<ServerResponse> userRoutes(Handler handler) {
        final String USERS_PATH = "/api/users";

        return route()
                .POST(USERS_PATH, handler::saveUseCase)
                .build();
    }
}