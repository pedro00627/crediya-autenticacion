package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.dto.response.UserResponseRecord;
import co.com.pragma.api.exception.dto.ErrorBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface UserApi {
    @Operation(
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
    Mono<ServerResponse> saveUseCase(ServerRequest serverRequest);

    @Operation(operationId = "obtener un usuario por email", summary = "Consultar un usuario", description = "Consultar un nuevo en el sistema.", parameters = @Parameter(name = "email", description = "Email del usuario a buscar.", required = true, in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), responses = {
            @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = UserResponseRecord.class))),
            @ApiResponse(responseCode = "400", description = "Petición inválida (ej. datos faltantes, formato incorrecto)", content = @Content(schema = @Schema(implementation = ErrorBody.class))),
            @ApiResponse(responseCode = "404", description = "Usuario o Documento no encontrados", content = @Content(schema = @Schema(implementation = ErrorBody.class)))
    })
    Mono<ServerResponse> getUserByEmail(ServerRequest serverRequest);

    @Operation(
            operationId = "obtener un usuario por email o documento de identidad",
            summary = "Consultar un usuario por email o documento de identidad",
            description = "Consultar un usuario en el sistema por email o documento",
            parameters = {
                    @Parameter(name = "email", description = "Email del usuario a buscar.", required = true, in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
                    @Parameter(name = "identityDocument", description = "Email del usuario a buscar.", required = true, in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = UserResponseRecord.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida (ej. datos faltantes, formato incorrecto)", content = @Content(schema = @Schema(implementation = ErrorBody.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario o Documento no encontrados", content = @Content(schema = @Schema(implementation = ErrorBody.class)))
            })
    Mono<ServerResponse> getUserByEmailOrIdentityDocument(ServerRequest serverRequest);
}
