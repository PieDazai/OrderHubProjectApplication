package project.ivanov.orderservice.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.topic.order-create-topic}")
    private String orderCreateTopic;

    @Bean
    public NewTopic orderCreateTopic() {
        return TopicBuilder.name(orderCreateTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
