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
                                            JwtAccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
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
                        .requestMatchers(HttpMethod.GET, "/users/*", "/users/*/with-cards", "/users/*/cards", "/cards/*")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/*")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/cards")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/cards/*")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/cards/*/activate", "/cards/*/deactivate")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/cards/*")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/users", "/cards")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/activate", "/users/*/deactivate")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
