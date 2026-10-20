package com.mallcloud.api.payment;

import com.mallcloud.api.payment.dto.CreatePaymentDTO;
import com.mallcloud.api.payment.dto.PaymentDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付服务内部接口。
 */
@FeignClient(name = "mall-payment", path = "/payment/inner", contextId = "paymentFeignClient")
public interface PaymentFeignClient {

    @PostMapping("/create")
    R<PaymentDTO> create(@RequestBody CreatePaymentDTO dto);

    @GetMapping("/detail/{orderNo}")
    R<PaymentDTO> getByOrderNo(@PathVariable("orderNo") String orderNo);
}
