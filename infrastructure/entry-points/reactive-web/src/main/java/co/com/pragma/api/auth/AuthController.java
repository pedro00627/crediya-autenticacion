package co.com.pragma.api.auth;

import co.com.pragma.api.utils.JWTUtil;
import co.com.pragma.model.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import co.com.pragma.model.security.PasswordEncryptor;

@RestController
@RequestMapping("/auth")
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
        return userRepository.getUserByEmail(authRequest.username())
                .flatMap(user -> {
                    if (passwordEncryptor.matches(authRequest.password(), user.password())) {
                        String token = jwtUtil.generateToken(user.email());
                        return Mono.just(ResponseEntity.ok(token));
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid credentials"));
                    }
                })
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("Invalid credentials")));
    }
}

// DTO for authentication request
record AuthRequest (
     String username,
     String password
){}
