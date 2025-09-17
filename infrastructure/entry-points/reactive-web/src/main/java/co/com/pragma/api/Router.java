package co.com.pragma.api;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Router {

    public static final String API_V_1_USUARIOS = "/api/v1/usuarios";
    public static final String API_V_1_USUARIOS_SEARCH = API_V_1_USUARIOS + "/search";
    public static final String SAVE_USE_CASE = "saveUseCase";
    public static final String GET_USER_BY_EMAIL = "getUserByEmail";

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = API_V_1_USUARIOS,
                            produces = {APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = Handler.class,
                            beanMethod = SAVE_USE_CASE
                    ),
                    @RouterOperation(
                            path = API_V_1_USUARIOS,
                            produces = {APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = Handler.class,
                            beanMethod = GET_USER_BY_EMAIL
                    )
            }

    )
    public RouterFunction<ServerResponse> userRoutes(Handler handler) {
        return route(POST(API_V_1_USUARIOS), handler::saveUseCase)
                .and(route(GET(API_V_1_USUARIOS), handler::getUserByEmail))
                .andRoute(GET(API_V_1_USUARIOS_SEARCH), handler::getUserByEmailOrIdentityDocument);
    }
}