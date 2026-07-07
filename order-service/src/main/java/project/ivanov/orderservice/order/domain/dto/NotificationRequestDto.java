package project.ivanov.orderservice.order.domain.dto;

import lombok.Builder;

@Builder
public record NotificationRequestDto(
        Long orderId,
        String eventType
) {
}
