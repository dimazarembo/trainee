package by.dzarembo.trainee.security;

import by.dzarembo.trainee.dto.ValidationTokenRequest;
import by.dzarembo.trainee.dto.ValidationTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {
    private final RestClient authRestClient;

    public ValidationTokenResponse validateToken(String token) {
        return authRestClient.post().uri("/auth/validate").contentType(MediaType.APPLICATION_JSON)
                .body(new ValidationTokenRequest(token)).retrieve().body(ValidationTokenResponse.class);

    }
}
