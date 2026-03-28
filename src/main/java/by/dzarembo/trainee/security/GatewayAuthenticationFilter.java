package by.dzarembo.trainee.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationEntryPointHandler authenticationEntryPointHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        if (userIdHeader == null && roleHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userIdHeader == null || roleHeader == null) {
            SecurityContextHolder.clearContext();
            authenticationEntryPointHandler.commence(
                    request,
                    response,
                    new BadCredentialsException("Missing authentication headers")
            );
            return;
        }

        try {
            Long userId = Long.valueOf(userIdHeader);
            String role = roleHeader.toUpperCase();

            if (!SecurityRoles.ADMIN.equals(role) && !SecurityRoles.USER.equals(role)) {
                SecurityContextHolder.clearContext();
                authenticationEntryPointHandler.commence(
                        request,
                        response,
                        new BadCredentialsException("Invalid authentication headers")
                );
                return;
            }

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (NumberFormatException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPointHandler.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid authentication headers", ex)
            );
        }
    }
}
