package co.com.pragma.api.config;

import co.com.pragma.security.api.SecurityRulesProvider;
import co.com.pragma.security.api.config.AuthorizationRule;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "security.rules")
public class SecurityRulesProperties implements SecurityRulesProvider {

    private List<AuthorizationRule> authorization = new ArrayList<>();

    public List<AuthorizationRule> getAuthorization() {
        return this.authorization;
    }

    public void setAuthorization(final List<AuthorizationRule> authorization) {
        this.authorization = authorization;
    }

    @Override
    public List<AuthorizationRule> authorization() {
        return this.authorization;
    }
}
