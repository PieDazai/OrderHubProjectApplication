package project.ivanov.paymentservice.dto;

public record PaymentResponseDto(

        Boolean isSuccessful,
        String message

) {
}