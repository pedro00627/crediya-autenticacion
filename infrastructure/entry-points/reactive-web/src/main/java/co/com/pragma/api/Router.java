package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.dto.response.UserResponseRecord;
import co.com.pragma.api.exception.dto.ErrorBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Router {
    @Bean
    @RouterOperations(
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    produces = {APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "saveUseCase",
                    operation = @Operation(
                            operationId = "saveUser",
                            summary = "Crear un nuevo usuario",
                            description = "Registra un nuevo usuario en el sistema. Valida la información de entrada y las reglas de negocio.",
                            requestBody = @RequestBody(
                                    description = "Datos del usuario a crear.",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UserRequestRecord.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = UserResponseRecord.class))),
                                    @ApiResponse(responseCode = "400", description = "Petición inválida (ej. datos faltantes, formato incorrecto)", content = @Content(schema = @Schema(implementation = ErrorBody.class))),
                                    @ApiResponse(responseCode = "409", description = "Conflicto de negocio (ej. usuario ya existe)", content = @Content(schema = @Schema(implementation = ErrorBody.class)))
                            }
                    )
            )
    )
    public RouterFunction<ServerResponse> userRoutes(Handler handler) {
        final String USERS_PATH = "/api/users";

        return route()
                .POST(USERS_PATH, handler::saveUseCase)
                .build();
    }
}