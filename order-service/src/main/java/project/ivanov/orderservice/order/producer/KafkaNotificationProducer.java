package project.ivanov.orderservice.order.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaNotificationProducer {

    @Value("${app.topic.order-create-topic}")
    private String orderCreateTopic;

    private final KafkaTemplate<String, Object> reliableKafkaTemplate;

    public void sendOrderCreateEvent(OrderCreateEvent event) {

        String key = String.valueOf(event.orderId());

        CompletableFuture<SendResult<String, Object>> future = reliableKafkaTemplate.send(orderCreateTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Событие успешно отправлено в топик: {}, partition: {}, offset: {}",
                        orderCreateTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Ошибка отправки сообщения в кафка по orderID: {}", event.orderId(), ex);
            }
        });
    }
}
