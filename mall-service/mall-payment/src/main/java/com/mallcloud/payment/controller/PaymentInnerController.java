package com.mallcloud.payment.controller;

import com.mallcloud.api.payment.dto.CreatePaymentDTO;
import com.mallcloud.api.payment.dto.PaymentDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口，路径与 {@link com.mallcloud.api.payment.PaymentFeignClient} 契约一一对应。
 */
@RestController
@RequestMapping("/payment/inner")
@RequiredArgsConstructor
public class PaymentInnerController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public R<PaymentDTO> create(@RequestBody CreatePaymentDTO dto) {
        return R.ok(paymentService.create(dto));
    }

    @GetMapping("/detail/{orderNo}")
    public R<PaymentDTO> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        return R.ok(paymentService.getByOrderNo(orderNo));
    }
}
