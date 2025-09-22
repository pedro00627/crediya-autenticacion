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
        return this.secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void setExpiration(final long expiration) {
        this.expiration = expiration;
    }

    public List<String> getExcludedPaths() {
        return this.excludedPaths;
    }

    public void setExcludedPaths(final List<String> excludedPaths) {
        this.excludedPaths = null != excludedPaths ? excludedPaths : new ArrayList<>();
    }

    // Implementación de JWTProperties interface
    @Override
    public String secret() {
        return this.secret;
    }

    @Override
    public long expiration() {
        return this.expiration;
    }

    @Override
    public List<String> excludedPaths() {
        return this.excludedPaths;
    }
}
