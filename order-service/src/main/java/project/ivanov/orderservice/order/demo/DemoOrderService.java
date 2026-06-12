package project.ivanov.orderservice.order.demo;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.ivanov.orderservice.order.CreateOrderRequest;
import project.ivanov.orderservice.order.domain.Order;
import project.ivanov.orderservice.order.domain.OrderItem;
import project.ivanov.orderservice.order.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoOrderService {

    private final OrderRepository orderRepository;


    public Order saveOrderWithJdbc(CreateOrderRequest order) {
        List<OrderItem> items = order.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                )).toList();

        Order orderSaved = new Order(items);

        return orderRepository.save(orderSaved);
    }
}
