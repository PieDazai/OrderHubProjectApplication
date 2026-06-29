package project.ivanov.notificationservice;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {


    @PostMapping
    @SneakyThrows
    public ResponseEntity<Object> notify(@RequestBody NotificationRequest request) {

        log.info("Received request for notification: {}", request);

        int random = new Random().nextInt(100);

        log.info("Выпало число {}", random);

        if (random < 30) {
            log.error("Проблемы с отправкой информацией по заказу {}", request.orderId());
            throw new RuntimeException("Возникли проблемы с отправкой информацией по заказу");
        }

        if (random > 70) {
            log.warn("NotificationController замедлился");
            Thread.sleep(400);
        }

        return ResponseEntity.ok().build();
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> NotificationController LOADED <<<");
    }
}
