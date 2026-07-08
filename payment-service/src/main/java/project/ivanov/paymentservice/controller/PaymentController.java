package project.ivanov.paymentservice.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.ivanov.paymentservice.dto.PaymentRequestDto;
import project.ivanov.paymentservice.dto.PaymentResponseDto;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("api/v1/payment")
@Slf4j
public class PaymentController {

    private final AtomicBoolean failureMode = new AtomicBoolean(false);
    private final Random random = new Random();

    @PostMapping("/process")
    @SneakyThrows
    public ResponseEntity<PaymentResponseDto> processPayment(
            @RequestBody PaymentRequestDto request
    ) {
        log.info("Обработка оплаты для заказа id: {}, сумма: {}",
                request.orderId(), request.amount());

        if (failureMode.get()) {

            int randomRequest = random.nextInt(100);

            if (randomRequest < 60) {
//                log.error("Симуляция ошибки оплаты заказа id: {}, random: {}",
//                        request.orderId(), randomRequest);
//
//                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                        .body(new PaymentResponseDto(
//                                false,
//                                "Сервис оплаты недоступен")
//                        );

                log.error("Симуляция замедления оплаты заказа id: {}", request.orderId());

                Thread.sleep(5000);
              }
        }

        return ResponseEntity.ok()
                .body(new PaymentResponseDto(
                        true,
                        "Оплата прошла успешно")
                );
    }

    @GetMapping("admin/failure-mode")
    public void setFailureMode(@RequestParam boolean enabled) {
        failureMode.set(enabled);

        log.info("Failure mode в Payment сервисе перевключен на {}",  enabled);
    }
}
