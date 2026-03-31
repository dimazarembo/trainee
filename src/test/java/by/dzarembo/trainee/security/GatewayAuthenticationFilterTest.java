package by.dzarembo.trainee.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GatewayAuthenticationFilterTest {

    @Mock
    private AuthenticationEntryPointHandler authenticationEntryPointHandler;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private GatewayAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueFilterChain_whenAuthenticationHeadersMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationEntryPointHandler);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldCallEntryPoint_whenUserIdHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Role", SecurityRoles.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(99L, SecurityRoles.ADMIN),
                        null,
                        List.of()
                )
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(request, response);
        ArgumentCaptor<BadCredentialsException> exceptionCaptor = ArgumentCaptor.forClass(BadCredentialsException.class);
        verify(authenticationEntryPointHandler).commence(
                eq(request),
                eq(response),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue()).hasMessage("Missing authentication headers");
    }

    @Test
    void doFilterInternal_shouldCallEntryPoint_whenRoleHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(99L, SecurityRoles.ADMIN),
                        null,
                        List.of()
                )
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(request, response);
        ArgumentCaptor<BadCredentialsException> exceptionCaptor = ArgumentCaptor.forClass(BadCredentialsException.class);
        verify(authenticationEntryPointHandler).commence(
                eq(request),
                eq(response),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue()).hasMessage("Missing authentication headers");
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenHeadersAreValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "7");
        request.addHeader("X-User-Role", SecurityRoles.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new AuthenticatedUser(7L, SecurityRoles.USER));
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(SecurityRoles.ROLE_USER);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationEntryPointHandler);
    }

    @Test
    void doFilterInternal_shouldCallEntryPoint_whenRoleHeaderIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "7");
        request.addHeader("X-User-Role", "SUPERADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(99L, SecurityRoles.ADMIN),
                        null,
                        List.of()
                )
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(request, response);
        ArgumentCaptor<BadCredentialsException> exceptionCaptor = ArgumentCaptor.forClass(BadCredentialsException.class);
        verify(authenticationEntryPointHandler).commence(
                eq(request),
                eq(response),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue()).hasMessage("Invalid authentication headers");
    }

    @Test
    void doFilterInternal_shouldCallEntryPoint_whenUserIdHeaderIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "invalid-id");
        request.addHeader("X-User-Role", SecurityRoles.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(99L, SecurityRoles.ADMIN),
                        null,
                        List.of()
                )
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(request, response);
        ArgumentCaptor<BadCredentialsException> exceptionCaptor = ArgumentCaptor.forClass(BadCredentialsException.class);
        verify(authenticationEntryPointHandler).commence(
                eq(request),
                eq(response),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue()).hasMessage("Invalid authentication headers");
        assertThat(exceptionCaptor.getValue().getCause()).isInstanceOf(NumberFormatException.class);
    }
}
