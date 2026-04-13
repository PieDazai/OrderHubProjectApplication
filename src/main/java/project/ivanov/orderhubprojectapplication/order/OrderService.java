package project.ivanov.orderhubprojectapplication.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ivanov.orderhubprojectapplication.order.exception.NotFoundOrderException;
import project.ivanov.orderhubprojectapplication.order.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                )).collect(Collectors.toList());

        Order order = new Order(items);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id){
        return orderRepository.findWithItemById(id).orElseThrow(
                () -> new NotFoundOrderException("Order with id " + id + " not found"));
    }
}
