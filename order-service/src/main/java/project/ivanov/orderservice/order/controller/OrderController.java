package project.ivanov.orderservice.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.ivanov.orderservice.order.domain.dto.CreateOrderRequestDto;
import project.ivanov.orderservice.order.domain.dto.OrderResponseDto;
import project.ivanov.orderservice.order.service.OrderService;

import java.net.URI;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request
    ) {
        var order = orderService.createOrder(request);
        var response = OrderResponseDto.from(order);
        return ResponseEntity.created(URI.create("/orders/" + order.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id){
        var found = orderService.findById(id);
        var response = OrderResponseDto.from(found);
        return ResponseEntity.ok(response);
    }

    @GetMapping("admin/failure-mode")
    public void setFailureMode(@RequestParam boolean enabled) {
        orderService.setFailureMode(enabled);
    }
}
