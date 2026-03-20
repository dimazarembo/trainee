package by.dzarembo.trainee.security;

import by.dzarembo.trainee.dto.ValidationTokenResponse;
import by.dzarembo.trainee.exception.InvalidAuthTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TokenValidationService {
    private final AuthServiceClient authServiceClient;

    public AuthenticatedUser validateAccessToken(String token) {
        ValidationTokenResponse response;
        try {
            response = authServiceClient.validateToken(token);
        } catch (RestClientException e) {
            throw new InvalidAuthTokenException("Token validation failed");
        }

        if (response == null || !response.isValid() || !"access".equals(response.getTokenType())) {
            throw new InvalidAuthTokenException("Token invalid");
        }

        return new AuthenticatedUser(response.getUserId(), response.getRole());
    }
}
