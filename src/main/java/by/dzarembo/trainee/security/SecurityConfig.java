package by.dzarembo.trainee.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClient;

@Configuration
public class SecurityConfig {

    private static final String USER_BY_ID_PATH = "/users/*";
    private static final String USER_WITH_CARDS_PATH = "/users/*/with-cards";
    private static final String USER_CARDS_PATH = "/users/*/cards";
    private static final String USER_ACTIVATE_PATH = "/users/*/activate";
    private static final String USER_DEACTIVATE_PATH = "/users/*/deactivate";
    private static final String CARD_BY_ID_PATH = "/cards/*";
    private static final String CARD_ACTIVATE_PATH = "/cards/*/activate";
    private static final String CARD_DEACTIVATE_PATH = "/cards/*/deactivate";

    @Bean
    RestClient authRestClient(AuthServiceProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.url())
                .build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                            JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, USER_BY_ID_PATH, USER_WITH_CARDS_PATH, USER_CARDS_PATH, CARD_BY_ID_PATH)
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, USER_BY_ID_PATH)
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/cards")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, CARD_BY_ID_PATH)
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, CARD_ACTIVATE_PATH, CARD_DEACTIVATE_PATH)
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, CARD_BY_ID_PATH)
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/users", "/cards")
                        .hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/users")
                        .hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PATCH, USER_ACTIVATE_PATH, USER_DEACTIVATE_PATH)
                        .hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, USER_BY_ID_PATH)
                        .hasRole(SecurityRoles.ADMIN)
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
