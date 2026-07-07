package project.ivanov.orderservice.order.service;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ivanov.orderservice.order.domain.dto.CreateOrderRequestDto;
import project.ivanov.orderservice.order.client.PaymentResponseDto;
import project.ivanov.orderservice.order.domain.OrderItem;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;
import project.ivanov.orderservice.order.exception.NotFoundOrderException;
import project.ivanov.orderservice.order.exception.OrderCreationException;
import project.ivanov.orderservice.order.metrics.annotation.BusinessMetric;
import project.ivanov.orderservice.order.repository.OrderRepository;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService  paymentService;

    private final AtomicBoolean failureMode = new AtomicBoolean(false);
    private final Random random = new Random();

    @SneakyThrows
    @Transactional
    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create","type=write"}
    )
    @Observed(name = "order.creation", contextualName = "create-order")
    public Order createOrder(CreateOrderRequestDto request) {

        if (failureMode.get()) {
            int random = new Random().nextInt(100);

            log.info("Выпало число {}", random);

            if (random < 30) {
                log.error("Проблемы с обработкой заказа {}", random);
                throw new RuntimeException("Возникли проблемы с обработкой заказа");
            }

            if (random > 70) {
                log.warn("OrderService замедлился");
                Thread.sleep(300);
            }
        }

        log.info("Request to Create Order : {}", request);
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                )).collect(Collectors.toList());

        Order order = new Order(items);

        Order savedOrder = orderRepository.saveAndFlush(order);

        PaymentResponseDto dto = paymentService.processPaymentResilience4j(savedOrder);

        if (dto != null && !dto.isSuccessful()) {
            log.warn("Бизнес ошибка оплаты: {}", dto.message());
        }

        log.info("Order ready to send id: {}", savedOrder.getId());

        try {
            MDC.put("order_id", savedOrder.getId().toString());
            MDC.put("total_amount", savedOrder.getTotalPrice().toString());
            MDC.put("order_status", savedOrder.getStatus().toString());


            eventPublisher.publishEvent(
                    OrderCreateEvent.of(
                    savedOrder.getId(),
                    MDC.getCopyOfContextMap())
            );

            log.info("Successful to Create Order : {}", order);

            Span.current().setAttribute("order.id", savedOrder.getId());

            return savedOrder;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            log.error("Ошибка при оформлении заказа {}", cause.getMessage());
            throw new OrderCreationException("Error: " + cause.getMessage());
        } finally {
            MDC.remove("order_id");
            MDC.remove("total_amount");
            MDC.remove("order_status");
        }
    }

    @BusinessMetric(
            value = "orders.retrieved",
            tags = {"operation=get","type=read"}
    )
    @Transactional(readOnly = true)
    public Order findById(Long id){
        log.info("Request to try to get Order with id : {}", id);
        var order = orderRepository.findWithItemById(id).orElseThrow(
                () -> new NotFoundOrderException("Order with id " + id + " not found"));

        log.info("Successful to find Order with id: {}", order);
        return order;
    }

    public void setFailureMode(boolean enabled) {
        failureMode.set(enabled);

        log.info("Failure mode в Payment сервисе перевключен на {}",  enabled);
    }
}
