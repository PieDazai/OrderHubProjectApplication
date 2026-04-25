package project.ivanov.orderservice.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import project.ivanov.orderhubprojectapplication.order.OrderResponse;

import java.net.URI;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        var order = orderService.createOrder(request);
        var response = OrderResponse.from(order);
        return ResponseEntity.created(URI.create("/orders/" + order.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id){
        var found = orderService.findById(id);
        var response = OrderResponse.from(found);
        return ResponseEntity.ok(response);
    }
}
