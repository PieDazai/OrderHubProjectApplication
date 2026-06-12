package project.ivanov.orderservice.order.demo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.ivanov.orderservice.order.CreateOrderRequest;
import project.ivanov.orderservice.order.OrderResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo/orders")
public class OrderDemoController {

    private final DemoOrderService  demoOrderService;

    @PostMapping("/jdbc")
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request
            ){
        var saved = demoOrderService.saveOrderWithJdbc(request);
        var response = OrderResponse.from(saved);

        return ResponseEntity.ok(response);
    }
}
