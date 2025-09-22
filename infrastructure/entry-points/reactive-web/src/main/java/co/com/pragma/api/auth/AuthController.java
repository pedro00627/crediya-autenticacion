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

    public AuthController(final JWTUtil jwtUtil, final UserRepository userRepository, final PasswordEncryptor passwordEncryptor,
                          final LoggerPort logger, final RoleStrategyContext roleStrategyContext) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
        this.logger = logger;
        this.roleStrategyContext = roleStrategyContext;
    }

    @PostMapping(value = ApiConstants.LOGIN_PATH, produces = HttpConstants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody final AuthRequest authRequest) {
        this.logger.info("Login attempt for username: {}", authRequest.username());
        return this.userRepository.getUserByEmail(authRequest.username())
                .doOnNext(user -> this.logger.debug("User found: {}", user.email()))
                .filter(user -> {
                    final boolean matches = this.passwordEncryptor.matches(authRequest.password(), user.password());
                    this.logger.debug("Password match for user {}: {}", user.email(), matches);
                    return matches;
                })
                .map(user -> {
                    this.logger.debug("Mapping roles for user: {}", user.email());
                    final List<String> roles = this.mapRoleIdToRoleName(user.roleId());
                    this.logger.debug("Roles mapped: {}", roles);
                    final String token = this.jwtUtil.generateToken(user.email(), roles);
                    this.logger.info("Token generated for user {}: {}", user.email(), token.substring(0, Math.min(token.length(), 20)) + "...");
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    this.logger.warn("Login failed for username: {}. Invalid credentials.", authRequest.username());
                    final ResponseEntity<Map<String, String>> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", ErrorMessages.INVALID_CREDENTIALS));
                    return Mono.just(responseEntity);
                }))
                .onErrorResume(e -> {
                    this.logger.error("An unexpected error occurred during login for username {}: {}", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", ErrorMessages.INTERNAL_SERVER_ERROR)));
                });
    }

    private List<String> mapRoleIdToRoleName(final Integer roleId) {
        this.logger.debug("Mapping roles for roleId: {}", roleId);
        if (null == roleId) {
            this.logger.debug("Role ID is null, returning empty roles list.");
            return List.of();
        }
        return this.roleStrategyContext.getRolesForUser(roleId);
    }
}

record AuthRequest(
        String username,
        String password
) {
}
