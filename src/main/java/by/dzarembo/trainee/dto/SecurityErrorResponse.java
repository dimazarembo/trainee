package by.dzarembo.trainee.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@Builder
public class SecurityErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}

