package by.dzarembo.trainee.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.service")
public record AuthServiceProperties(String url) {
}
