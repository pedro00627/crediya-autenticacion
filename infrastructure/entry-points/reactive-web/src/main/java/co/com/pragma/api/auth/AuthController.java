package co.com.pragma.api.auth;

import co.com.pragma.api.utils.JWTUtil;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.security.RoleConstants;
import co.com.pragma.model.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncryptor passwordEncryptor;

    public AuthController(JWTUtil jwtUtil, UserRepository userRepository, PasswordEncryptor passwordEncryptor) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody AuthRequest authRequest) {
        return userRepository.getUserByEmail(authRequest.username())
                .filter(user -> passwordEncryptor.matches(authRequest.password(), user.password()))
                .map(user -> {
                    List<String> roles = mapRoleIdToRoleName(user.roleId());
                    String token = jwtUtil.generateToken(user.email(), roles);
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Credenciales inválidas"))
                ));
    }

    private List<String> mapRoleIdToRoleName(Integer roleId) {
        if (roleId == null) {
            return List.of();
        }
        return switch (roleId) {
            case 1 -> List.of(RoleConstants.ROLE_ADMIN);
            case 2 -> List.of(RoleConstants.ROLE_ADVISOR);
            case 3 -> List.of(RoleConstants.ROLE_CLIENT);
            default -> List.of();
        };
    }
}

record AuthRequest (
    String username,
    String password
){}
