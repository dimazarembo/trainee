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
public class UserResponse {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
