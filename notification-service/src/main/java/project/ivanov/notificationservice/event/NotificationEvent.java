package project.ivanov.notificationservice.event;

import lombok.Builder;

@Builder
public record NotificationEvent(
        Long orderId,
        String transactionId,
        String status,
        String reason
) {
}