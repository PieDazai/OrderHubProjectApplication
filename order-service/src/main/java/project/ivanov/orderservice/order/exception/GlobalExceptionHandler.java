package project.ivanov.orderservice.order.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.ivanov.orderservice.order.domain.dto.ErrorDto;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> globalExceptionHandler(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .toList();

        var error = new ErrorDto(
                ex.getStatusCode().value(),
                "Validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(NotFoundOrderException.class)
    public ResponseEntity<ErrorDto> globalExceptionHandler(NotFoundOrderException ex) {

        var error =  new ErrorDto(
                404,
                "Order not found",
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorDto> handleException(PaymentFailedException ex) {

        ErrorDto error = new ErrorDto(500, "Ошибка приложения", ex.getMessage());

        return ResponseEntity.internalServerError().body(error);

    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<ErrorDto> handleException(OrderCreationException ex) {

        ErrorDto error = new ErrorDto(500, "Ошибка оформления заказа", ex.getMessage());

        return ResponseEntity.internalServerError().body(error);

    }

}
