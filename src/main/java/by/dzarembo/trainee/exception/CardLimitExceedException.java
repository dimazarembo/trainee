package by.dzarembo.trainee.exception;

public class CardLimitExceedException extends RuntimeException {
    public CardLimitExceedException(String message) {
        super(message);
    }
}
