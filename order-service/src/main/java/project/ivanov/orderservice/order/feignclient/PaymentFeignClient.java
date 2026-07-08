package project.ivanov.orderservice.order.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import project.ivanov.orderservice.order.config.FeignConfig;
import project.ivanov.orderservice.order.domain.dto.PaymentRequestDto;
import project.ivanov.orderservice.order.domain.dto.PaymentResponseDto;

@FeignClient(
        name = "payment-service",
        url = "${url.payment-service:http://payment-service:8083}",
        configuration = FeignConfig.class
)
public interface PaymentFeignClient {

    @PostMapping("/api/v1/payment/process")
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto);
}
