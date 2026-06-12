package project.ivanov.orderservice.order;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ivanov.orderservice.order.domain.OrderItem;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.domain.event.OrderCreateEvent;
import project.ivanov.orderservice.order.exception.NotFoundOrderException;
import project.ivanov.orderservice.order.metrics.annotation.BusinessMetric;
import project.ivanov.orderservice.order.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create","type=write"}
    )
    @Observed(name = "order.creation", contextualName = "create-order")
    public Order createOrder(CreateOrderRequest request) {
        log.info("Request to Create Order : {}", request);
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                )).collect(Collectors.toList());

        Order order = new Order(items);

        Order savedOrder = orderRepository.save(order);

        log.info("Order ready to send id: {}", savedOrder.getId());

        eventPublisher.publishEvent(OrderCreateEvent.of(savedOrder.getId()));

        log.info("Successful to Create Order : {}", order);

        Span.current().setAttribute("order.id", savedOrder.getId());

        return savedOrder;
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
}
