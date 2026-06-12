package project.ivanov.orderservice.order.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.ivanov.orderservice.order.domain.NotificationRequest;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final WebClient notificationWebClient;

    public void notifyOrderCreate(Long orderId){

        NotificationRequest request = NotificationRequest.builder()
                .orderId(orderId)
                .eventType("CREATED")
                .build();

        notificationWebClient.post()
                .uri("/api/notifications")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Notification error: " + body))
                )
                .toBodilessEntity()
                .block();

    }


}
