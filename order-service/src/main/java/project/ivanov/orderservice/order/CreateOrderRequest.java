package project.ivanov.orderservice.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(

        @Valid
        @NotEmpty(message = "items not be is empty")
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(

            @NotNull(message = "productId not be is empty")
            Long productId,

            @NotBlank(message = "productName must be not null")
            String productName,

            @Min(value = 1, message = "min value for quantity 1")
            int quantity,

            @NotNull(message = "price must be not null")
            @DecimalMin(value = "0.01", message = "min value for price 0.01")
            BigDecimal price
    ){}
}
