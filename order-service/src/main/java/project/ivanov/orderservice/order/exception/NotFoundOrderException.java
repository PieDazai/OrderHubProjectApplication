package project.ivanov.orderservice.order.exception;

public class NotFoundOrderException extends RuntimeException {
    public NotFoundOrderException(String message) {
        super(message);
    }
}
