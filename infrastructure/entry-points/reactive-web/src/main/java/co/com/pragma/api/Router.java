package co.com.pragma.api;

import co.com.pragma.model.constants.ApiConstants;
import co.com.pragma.model.constants.HttpConstants;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Router {

    // Method names for documentation
    public static final String SAVE_USE_CASE = "saveUseCase";
    public static final String GET_USER_BY_EMAIL = "getUserByEmail";

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = ApiConstants.USERS_ENDPOINT,
                            produces = {HttpConstants.APPLICATION_JSON},
                            method = RequestMethod.POST,
                            beanClass = Handler.class,
                            beanMethod = SAVE_USE_CASE
                    ),
                    @RouterOperation(
                            path = ApiConstants.USERS_ENDPOINT,
                            produces = {HttpConstants.APPLICATION_JSON},
                            method = RequestMethod.GET,
                            beanClass = Handler.class,
                            beanMethod = GET_USER_BY_EMAIL
                    )
            }

    )
    public RouterFunction<ServerResponse> userRoutes(Handler handler) {
        return route(POST(ApiConstants.USERS_ENDPOINT), handler::saveUseCase)
                .and(route(GET(ApiConstants.USERS_ENDPOINT), handler::getUserByEmail))
                .andRoute(GET(ApiConstants.USERS_SEARCH_ENDPOINT), handler::getUserByEmailOrIdentityDocument);
    }
}