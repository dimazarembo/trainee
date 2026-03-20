package by.dzarembo.trainee.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithCardsResponse {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UserCardInfoResponse> cards;
}
