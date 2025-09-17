package co.com.pragma.api.config;

import co.com.pragma.security.api.JWTProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "jwt")
public class SecurityProperties implements JWTProperties {

    private String secret;
    private long expiration;
    private List<String> excludedPaths = new ArrayList<>();

    public SecurityProperties() {
        // Constructor por defecto requerido por Spring
    }

    // Getters y setters para Spring Boot Configuration Properties
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths != null ? excludedPaths : new ArrayList<>();
    }

    // Implementación de JWTProperties interface
    @Override
    public String secret() {
        return secret;
    }

    @Override
    public long expiration() {
        return expiration;
    }

    @Override
    public List<String> excludedPaths() {
        return excludedPaths;
    }
}
