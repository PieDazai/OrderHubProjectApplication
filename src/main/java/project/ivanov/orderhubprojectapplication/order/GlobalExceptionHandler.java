package project.ivanov.orderhubprojectapplication.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> globalExceptionHandler(MethodArgumentNotValidException ex) {

        List<String> errors = ex.   getBindingResult()
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
}
