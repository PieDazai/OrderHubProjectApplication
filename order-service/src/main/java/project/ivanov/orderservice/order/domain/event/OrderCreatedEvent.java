package project.ivanov.orderservice.order.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderCreateEvent(
        Long orderId,
        Map<String, String> context,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime timestamp
) {
    public static OrderCreateEvent of(
            Long orderId,
            Map<String, String> context
    ){
        return new OrderCreateEvent(
                orderId,
                context,
                LocalDateTime.now()
        );
    }
}
