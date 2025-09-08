package co.com.pragma.api.auth;

import co.com.pragma.api.utils.JWTUtil;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.security.RoleConstants;
import co.com.pragma.model.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody AuthRequest authRequest) {
        return userRepository.getUserByEmail(authRequest.getUsername())
                .flatMap(user -> {
                    if (passwordEncryptor.matches(authRequest.getPassword(), user.password())) {
                        List<String> roles = mapRoleIdToRoleName(user.roleId());
                        String token = jwtUtil.generateToken(user.email(), roles);
                        return Mono.just(ResponseEntity.ok(token));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
                    }
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")));
    }

    private List<String> mapRoleIdToRoleName(Integer roleId) {
        if (roleId == null) {
            return List.of();
        }
        switch (roleId) {
            case 1:
                return List.of(RoleConstants.ROLE_ADMIN);
            case 2:
                return List.of(RoleConstants.ROLE_ADVISOR);
            case 3:
                return List.of(RoleConstants.ROLE_CLIENT);
            default:
                return List.of();
        }
    }
}

class AuthRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
