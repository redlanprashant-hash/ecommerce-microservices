package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.PaymentRequestDto;
import com.ecommerce.orderservice.dto.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

    @PostMapping("/api/payments")
    PaymentResponseDto createPayment(@RequestHeader("X-User-Id") Long userId, @RequestBody PaymentRequestDto requestDto);

}
