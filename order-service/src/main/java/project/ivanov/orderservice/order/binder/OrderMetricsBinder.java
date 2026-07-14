package project.ivanov.orderservice.order.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import project.ivanov.orderservice.order.dictionary.OrderStatus;
import project.ivanov.orderservice.order.repository.OrderRepository;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMetricsBinder implements MeterBinder {

    private final OrderRepository orderRepository;

    @Override
    public void bindTo(MeterRegistry registry) {

        for (OrderStatus orderStatus : OrderStatus.values()) {
            Gauge.builder("orders.status.count", orderRepository,
                            orderRepository -> orderRepository.countByStatus(orderStatus))
                    .tags("order.status", orderStatus.name())
                    .description("Orders count for status " + orderStatus)
                    .register(registry);
        }
    }
}
