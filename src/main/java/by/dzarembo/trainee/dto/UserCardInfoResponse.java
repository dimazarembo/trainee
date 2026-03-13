package by.dzarembo.trainee.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardInfoResponse {
    private Long id;
    private String cardNumber;
    private String holderName;
    private LocalDate expirationDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
