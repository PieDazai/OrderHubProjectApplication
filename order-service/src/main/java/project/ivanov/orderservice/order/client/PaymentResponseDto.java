package project.ivanov.orderservice.order.client;

import lombok.Builder;

@Builder
public record PaymentResponseDto(

        Boolean isSuccessful,
        String message,
        boolean requiresPendingProcessing

) {
}