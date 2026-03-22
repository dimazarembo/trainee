package by.dzarembo.trainee.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationTokenResponse {
    private boolean valid;
    private Long userId;
    private String role;
    private String tokenType;
}
