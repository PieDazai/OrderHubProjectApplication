package project.ivanov.orderservice.order.service;

import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
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
import project.ivanov.orderservice.order.domain.dto.PaymentResponseDto;
import project.ivanov.orderservice.order.domain.dto.PaymentRequestDto;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.exception.PaymentFailedException;
import project.ivanov.orderservice.order.feignclient.PaymentFeignClient;

import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;
    private final PaymentFeignClient paymentFeignClient;

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
    public PaymentResponseDto processPaymentStandardSpring(Order order) {
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
    @TimeLimiter(name = "paymentService")
    @Retry(name = "paymentService")
    @Bulkhead(name = "paymentService")
    public CompletableFuture<PaymentResponseDto> processPaymentResilience4j(Order order) {

        return paymentClient
                .processPayment(order.getId(), order.getTotalPrice())
                .toFuture()
                .exceptionally( throwable -> {

                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;

                    log.error("Техническая ошибка при вызове payment-service: {}", cause.getMessage());

                    throw new PaymentFailedException("Payment service error: " + cause.getMessage());

                });
    }

    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "fallbackPaymentProcess"
    )
    @Retry(name = "paymentService")
    @Bulkhead(
            name = "paymentService",
            fallbackMethod = "fallbackBulkheadPayment"
    )
    public PaymentResponseDto processPaymentFeign(Order savedOrder) {
        try {
            PaymentResponseDto response = paymentFeignClient.processPayment(
                    new PaymentRequestDto(savedOrder.getId(), savedOrder.getTotalPrice())
            );

            log.debug("Успешно обработана оплата через feign-клиента. Ответ: {}", response);

            return response;

        } catch (FeignException.FeignClientException e) {
            log.error("Техническая ошибка при вызове payment-service через feign-клиента");
            throw new PaymentFailedException("Payment service error: " + e.getMessage());
        }
    }

    public CompletableFuture<PaymentResponseDto> fallbackPaymentProcess(
                Order order,
                Throwable t
            ) {

        log.warn("Fallback для заказа: {}, по причине: {}", order.getId(), t.getMessage());

        String message = t.getMessage();

        return CompletableFuture.completedFuture(
                PaymentResponseDto.builder()
                        .message(message)
                        .requiresPendingProcessing(true)
                        .isSuccessful(true)
                        .build()
        );

        }
}