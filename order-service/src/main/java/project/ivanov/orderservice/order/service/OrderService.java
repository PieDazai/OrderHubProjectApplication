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
import project.ivanov.orderservice.order.dictionary.OrderStatus;
import project.ivanov.orderservice.order.domain.dto.CreateOrderRequestDto;
import project.ivanov.orderservice.order.domain.dto.PaymentResponseDto;
import project.ivanov.orderservice.order.domain.OrderItem;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;
import project.ivanov.orderservice.order.exception.NotFoundOrderException;
import project.ivanov.orderservice.order.exception.OrderCreationException;
import project.ivanov.orderservice.order.metrics.annotation.BusinessMetric;
import project.ivanov.orderservice.order.producer.NotificationProducer;
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
    private final NotificationProducer notificationProducer;
    private final PaymentService  paymentService;

    private final AtomicBoolean failureMode = new AtomicBoolean(false);
    private final Random random = new Random();

    @SneakyThrows
    @Transactional(timeout = 30)
    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create","type=write"}
    )
    @Observed(name = "order.creation", contextualName = "create-order")
    public Order createOrder(CreateOrderRequestDto request) {

        log.info("Получен запрос на создание заказа : {}", request);

        try {
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

            List<OrderItem> items = request.items().stream()
                    .map(item -> new OrderItem(
                            item.productId(),
                            item.productName(),
                            item.quantity(),
                            item.price()
                    )).collect(Collectors.toList());

            Order order = new Order(items);
            order.setStatus(OrderStatus.PENDING);

            Order savedOrder = orderRepository.saveAndFlush(order);

            log.info("Создан заказ, id: {}, со статусом PENDING", savedOrder.getId());

            //PaymentResponseDto response = paymentService.processPaymentFeign(savedOrder);

            PaymentResponseDto response = paymentService.processPaymentResilience4j(savedOrder).get();

            if (response.requiresPendingProcessing()) {
                log.info("Заказ id: {} не был сразу оплачен, пойдет на потоврную оплату", savedOrder.getId());
            } else if (response.isSuccessful()) {
                savedOrder.setStatus(OrderStatus.PAID);
                orderRepository.saveAndFlush(savedOrder);

                log.info("Заказ id: {} был сразу оплачен и сохранен", savedOrder.getId());

                log.info("Отправялем инфо о заказе: {}", savedOrder.getId());

                notificationProducer.sendOrderNotification(
                        OrderCreateEvent.of(
                                savedOrder.getId(),
                                MDC.getCopyOfContextMap())
                );
            } else {
                savedOrder.setStatus(OrderStatus.CANCELLED);
                orderRepository.saveAndFlush(savedOrder);

                log.warn("Бизнес ошибка оплаты заказа id: {}, причина: {}", savedOrder.getId(), response.message());
            }

            MDC.put("order_id", savedOrder.getId().toString());
            MDC.put("total_amount", savedOrder.getTotalPrice().toString());
            MDC.put("order_status", savedOrder.getStatus().toString());

            Span.current().setAttribute("order.id", savedOrder.getId());

            return savedOrder;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            log.error("Ошибка при оплате заказа {}", cause.getMessage());
            throw new OrderCreationException("Order created error: " + cause.getMessage());
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
        log.info("Запрос на получение заказа с id: {}", id);
        var order = orderRepository.findWithItemById(id).orElseThrow(
                () -> new NotFoundOrderException("Заказ с id " + id + " не найден"));

        log.info("Успешно найдет заказ с id: {}", order);
        return order;
    }

    public void setFailureMode(boolean enabled) {
        failureMode.set(enabled);

        log.info("Failure mode в Payment сервисе перевключен на {}",  enabled);
    }
}
