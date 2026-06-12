package project.ivanov.notificationservice;

public record NotificationRequest(
        Long orderId,
        String eventType
) {
}
