package project.ivanov.orderservice.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.ivanov.orderservice.order.domain.ErrorDto;
import project.ivanov.orderservice.order.exception.NotFoundOrderException;

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

}
