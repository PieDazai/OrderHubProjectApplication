package project.ivanov.orderservice.order.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import project.ivanov.orderservice.order.client.PaymentClient;
import project.ivanov.orderservice.order.client.PaymentResponseDto;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.exception.PaymentFailedException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;

    @Retryable(
            retryFor = {
                    WebClientResponseException.class,
                    PaymentFailedException.class,
            },
            noRetryFor = {
                    IllegalArgumentException.class,
            },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 200,
                    maxDelay = 500,
                    multiplier = 2,
                    random = true
            )
    )
    public PaymentResponseDto processPaymentDefault(Order order) {
        RetryContext context = RetrySynchronizationManager.getContext();

        int attempts = context != null ? context.getRetryCount() : 1;

        log.debug("ПОПЫТКА {} ИЗ 3 <======", attempts);

        try {
            PaymentResponseDto dto = paymentClient.processPayment(
                    order.getId(),
                    order.getTotalPrice()
            ).block();

            log.info("PaymentService попытка {} успешна для заказа id: {}",
                    attempts,
                    order.getId());

            return dto;
        } catch (WebClientResponseException e) {
            log.error("Попытка {}, Техниическая ошибка в Payment серивсе {}",
                    attempts, e.getMessage());
            throw new PaymentFailedException("Payment service error: " + e.getMessage());
        }
    }

    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "fallbackPaymentProcess"
    )
    @Retry(name = "paymentService")
    public PaymentResponseDto processPaymentResilience4j(Order order) {

        try {
            PaymentResponseDto dto = paymentClient.processPayment(
                    order.getId(),
                    order.getTotalPrice()
            ).block();

            log.info("PaymentService попытка успешна для заказа id: {}", order.getId());

            return dto;
        } catch (WebClientResponseException e) {
            log.error("Техниическая ошибка в Payment серивсе {}", e.getMessage());
            throw new PaymentFailedException("Payment service error: " + e.getMessage());
        }
    }

    @Recover
    public PaymentResponseDto fallbackPaymentProcess(Order order, Throwable t) {

        log.warn("Резервная логика для заказа {} после срабатвания Circuit Breaker: {}",
                order.getId(), t.getMessage());

        throw new PaymentFailedException("Payment service error: " + t.getMessage());


    }
}
