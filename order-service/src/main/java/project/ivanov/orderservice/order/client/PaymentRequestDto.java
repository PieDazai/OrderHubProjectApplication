package project.ivanov.orderservice.order.client;

import java.math.BigDecimal;

public record PaymentRequestDto(

        Long orderId,
        BigDecimal amount

) {
}
