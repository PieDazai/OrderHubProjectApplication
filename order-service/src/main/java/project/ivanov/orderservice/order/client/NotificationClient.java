package project.ivanov.orderservice.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.ivanov.orderservice.order.domain.NotificationRequest;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final WebClient notificationWebClient;

    @Async
    public void notifyOrderCreate(OrderCreateEvent event){

        Map<String, String> context = event.context();

        if (context != null ) {
            MDC.setContextMap(context);
        }

        long orderId = event.orderId();

        try {

            log.info("Отправка уведомления для заказа {}, traceId={}, total amount={}",
                    orderId,
                    MDC.get("traceId"),
                    MDC.get("total_amount")
            );

            NotificationRequest request = NotificationRequest.builder()
                    .orderId(orderId)
                    .eventType("CREATED")
                    .build();

            notificationWebClient.post()
                    .uri("/api/notifications")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } finally {
            MDC.clear();
        }

    }


}
