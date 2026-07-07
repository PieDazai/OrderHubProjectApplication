package project.ivanov.orderservice.order.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.ivanov.orderservice.order.client.NotificationClient;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationClient notificationClient;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreateEvent event){
        log.info("get event : {}", event);

        try{
            notificationClient.notifyOrderCreate(event);
            log.info("event was send, id : {}", event.orderId());
        } catch (Exception ex){
            log.error("failed to send event : {}", event, ex);
        }
    }
}
