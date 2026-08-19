package project.ivanov.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import project.ivanov.notificationservice.event.OrderCreatedEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(
                    delay = 1000,
                    maxDelay = 10000,
                    multiplier = 2.0,
                    random = true
            ),
            timeout = "6000",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = ".DLT",
            exclude = {NullPointerException.class},
            traversingCauses = "true",
            autoCreateTopics = "true",
            numPartitions = "3",
            replicationFactor = "1",
            listenerContainerFactory = "kafkaListenerContainerFactory"
    )
    @KafkaListener(
            topics = "${app.topic.order-create-topic}",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, byte[]> record, Acknowledgment ack) {

        log.info("Приянтно событие в консьюмер: {}", record.value());

        try {
            byte[] payload = record.value();

            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

            log.info("Иммитиация бизнес-логики отправки уведомления для заказа с orderId: {}", event.orderId());

            if (event.orderId() == null) {
                throw new NullPointerException("orderId is null");
            }

            log.info("Оффсет для заказа с orderId: {}, сдвинут", event.orderId());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка обработки события", e);
            throw new RuntimeException("Processing failed, will be restarted");
        }
    }
}
