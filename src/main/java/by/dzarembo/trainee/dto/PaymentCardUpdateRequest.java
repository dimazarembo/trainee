package by.dzarembo.trainee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCardUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String cardNumber;

    @NotBlank
    @Size(max = 100)
    private String holderName;

    @NotNull
    private LocalDate expirationDate;
}
