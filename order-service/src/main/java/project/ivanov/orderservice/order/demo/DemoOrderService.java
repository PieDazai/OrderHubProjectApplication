package project.ivanov.orderservice.order.demo;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.ivanov.orderhubprojectapplication.order.CreateOrderRequest;
import project.ivanov.orderhubprojectapplication.order.Order;
import project.ivanov.orderhubprojectapplication.order.OrderItem;
import project.ivanov.orderhubprojectapplication.order.repository.OrderRepository;

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
