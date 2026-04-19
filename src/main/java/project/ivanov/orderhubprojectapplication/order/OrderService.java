package project.ivanov.orderhubprojectapplication.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ivanov.orderhubprojectapplication.order.exception.NotFoundOrderException;
import project.ivanov.orderhubprojectapplication.order.metrics.annotation.BusinessMetric;
import project.ivanov.orderhubprojectapplication.order.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;


    @Transactional
    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create","type=write"}
    )
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

        log.info("Successful to Create Order : {}", order);
        return orderRepository.save(order);
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
