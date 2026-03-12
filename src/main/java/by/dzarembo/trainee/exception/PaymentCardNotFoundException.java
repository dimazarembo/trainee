package by.dzarembo.trainee.exception;

public class PaymentCardNotFoundException extends RuntimeException {
    public PaymentCardNotFoundException(String message) {
        super(message);
    }
}
