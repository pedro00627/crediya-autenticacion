package co.com.pragma.api.auth;

import co.com.pragma.api.auth.strategy.RoleStrategyContext;
import co.com.pragma.model.constants.ApiConstants;
import co.com.pragma.model.constants.ErrorMessages;
import co.com.pragma.model.constants.HttpConstants;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.security.util.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConstants.API_V1_BASE_PATH)
public class AuthController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncryptor passwordEncryptor;
    private final LoggerPort logger;
    private final RoleStrategyContext roleStrategyContext;

    public AuthController(JWTUtil jwtUtil, UserRepository userRepository, PasswordEncryptor passwordEncryptor,
                          LoggerPort logger, RoleStrategyContext roleStrategyContext) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
        this.logger = logger;
        this.roleStrategyContext = roleStrategyContext;
    }

    @PostMapping(value = ApiConstants.LOGIN_PATH, produces = HttpConstants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody AuthRequest authRequest) {
        logger.info("Login attempt for username: {}", authRequest.username());
        return userRepository.getUserByEmail(authRequest.username())
                .doOnNext(user -> logger.debug("User found: {}", user.email()))
                .filter(user -> {
                    boolean matches = passwordEncryptor.matches(authRequest.password(), user.password());
                    logger.debug("Password match for user {}: {}", user.email(), matches);
                    return matches;
                })
                .map(user -> {
                    logger.debug("Mapping roles for user: {}", user.email());
                    List<String> roles = mapRoleIdToRoleName(user.roleId());
                    logger.debug("Roles mapped: {}", roles);
                    String token = jwtUtil.generateToken(user.email(), roles);
                    logger.info("Token generated for user {}: {}", user.email(), token.substring(0, Math.min(token.length(), 20)) + "...");
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Login failed for username: {}. Invalid credentials.", authRequest.username());
                    ResponseEntity<Map<String, String>> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", ErrorMessages.INVALID_CREDENTIALS));
                    return Mono.just(responseEntity);
                }))
                .onErrorResume(e -> {
                    logger.error("An unexpected error occurred during login for username {}: {}", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", ErrorMessages.INTERNAL_SERVER_ERROR)));
                });
    }

    private List<String> mapRoleIdToRoleName(Integer roleId) {
        logger.debug("Mapping roles for roleId: {}", roleId);
        if (null == roleId) {
            logger.debug("Role ID is null, returning empty roles list.");
            return List.of();
        }
        return roleStrategyContext.getRolesForUser(roleId);
    }
}

record AuthRequest(
        String username,
        String password
) {
}
