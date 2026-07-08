package project.ivanov.orderservice.order.domain.dto;

import lombok.Builder;

@Builder
public record PaymentResponseDto(

        Boolean isSuccessful,
        String message,
        boolean requiresPendingProcessing

) {
}