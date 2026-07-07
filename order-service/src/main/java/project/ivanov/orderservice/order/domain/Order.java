package project.ivanov.orderservice.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ivanov.orderservice.order.dictionary.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "create_at")
    private Instant createAt;

    public Order(List<OrderItem> items) {
        this.items.addAll(items);
        this.createAt = Instant.now();
        this.status = OrderStatus.CREATED;
        this.orderNumber = UUID.randomUUID().toString();

        if(items != null) {
            items.forEach(item -> item.setOrder(this));
        }
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public BigDecimal getTotalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
