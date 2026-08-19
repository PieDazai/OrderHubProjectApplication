//package project.ivanov.orderservice.order.producer;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;
//
//import java.util.UUID;
//
//import static project.ivanov.orderservice.order.config.ProducerRabbitConfig.EXCHANGE_NOTIFICATIONS;
//import static project.ivanov.orderservice.order.config.ProducerRabbitConfig.ROUTING_KEY_NOTIFICATIONS;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class NotificationProducer {
//
//    private final RabbitTemplate rabbitTemplate;
//
//    public void sendOrderNotification(OrderCreateEvent event) {
//
//        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
//
//        log.info("Отправка OrderCreateEvent id: {}, correlation id: {}",
//                event.orderId(), correlationData.getId());
//
//        rabbitTemplate.convertAndSend(
//                EXCHANGE_NOTIFICATIONS,
//                ROUTING_KEY_NOTIFICATIONS,
//                event,
//                correlationData
//        );
//    }
//}
