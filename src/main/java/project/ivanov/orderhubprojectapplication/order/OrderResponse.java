package project.ivanov.orderhubprojectapplication.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        Instant createdAt,
        List<OrderItemResponse> items,
        BigDecimal total
) {
    public static OrderResponse from(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreateAt(),
                items,
                total
        );
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal price,
            BigDecimal itemTotal
            ) {

        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
    }
}
