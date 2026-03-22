package by.dzarembo.trainee.security;

import by.dzarembo.trainee.dto.ValidationTokenResponse;
import by.dzarembo.trainee.exception.InvalidAuthTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidationServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private TokenValidationService tokenValidationService;

    @Test
    void validateAccessToken_shouldReturnAuthenticatedUser_whenTokenIsValid() {
        ValidationTokenResponse response = ValidationTokenResponse.builder()
                .valid(true)
                .userId(7L)
                .role(SecurityRoles.USER)
                .tokenType("access")
                .build();

        when(authServiceClient.validateToken("valid-token")).thenReturn(response);

        AuthenticatedUser result = tokenValidationService.validateAccessToken("valid-token");

        assertThat(result).isEqualTo(new AuthenticatedUser(7L, SecurityRoles.USER));
        verify(authServiceClient).validateToken("valid-token");
    }

    @Test
    void validateAccessToken_shouldThrowInvalidAuthTokenException_whenAuthServiceThrowsRestClientException() {
        when(authServiceClient.validateToken("broken-token"))
                .thenThrow(new RestClientException("Service unavailable"));

        assertThatThrownBy(() -> tokenValidationService.validateAccessToken("broken-token"))
                .isInstanceOf(InvalidAuthTokenException.class)
                .hasMessage("Token validation failed");
    }

    @Test
    void validateAccessToken_shouldThrowInvalidAuthTokenException_whenResponseIsNull() {
        when(authServiceClient.validateToken("null-response-token")).thenReturn(null);

        assertThatThrownBy(() -> tokenValidationService.validateAccessToken("null-response-token"))
                .isInstanceOf(InvalidAuthTokenException.class)
                .hasMessage("Token invalid");
    }

    @Test
    void validateAccessToken_shouldThrowInvalidAuthTokenException_whenTokenIsMarkedInvalid() {
        ValidationTokenResponse response = ValidationTokenResponse.builder()
                .valid(false)
                .userId(7L)
                .role(SecurityRoles.USER)
                .tokenType("access")
                .build();

        when(authServiceClient.validateToken("invalid-token")).thenReturn(response);

        assertThatThrownBy(() -> tokenValidationService.validateAccessToken("invalid-token"))
                .isInstanceOf(InvalidAuthTokenException.class)
                .hasMessage("Token invalid");
    }

    @Test
    void validateAccessToken_shouldThrowInvalidAuthTokenException_whenTokenTypeIsNotAccess() {
        ValidationTokenResponse response = ValidationTokenResponse.builder()
                .valid(true)
                .userId(7L)
                .role(SecurityRoles.USER)
                .tokenType("refresh")
                .build();

        when(authServiceClient.validateToken("refresh-token")).thenReturn(response);

        assertThatThrownBy(() -> tokenValidationService.validateAccessToken("refresh-token"))
                .isInstanceOf(InvalidAuthTokenException.class)
                .hasMessage("Token invalid");
    }
}
