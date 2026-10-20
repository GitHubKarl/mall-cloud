package com.mallcloud.payment.controller;

import com.mallcloud.api.payment.dto.PaymentDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付对外接口（渠道回调 + 开发期模拟支付）。
 * <p>
 * 内部调用接口统一放在 {@link PaymentInnerController}，避免两个 Controller 出现重复路由。
 */
@Tag(name = "支付")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 渠道异步回调。公网可达，网关白名单放行，凭签名鉴权而非 JWT。
     */
    @Operation(summary = "支付渠道回调")
    @PostMapping("/callback/{channel}")
    public String callback(@PathVariable("channel") String channel,
                           @RequestParam Map<String, String> params) {
        boolean handled = paymentService.callback(channel, params);
        // 渠道要求返回固定格式：SUCCESS 表示已收到，否则会持续重试
        return handled ? "SUCCESS" : "FAIL";
    }

    @Operation(summary = "模拟支付成功（仅开发环境，生产必须关闭）")
    @PostMapping("/mock/{orderNo}")
    public R<PaymentDTO> mockPay(@PathVariable("orderNo") String orderNo) {
        return R.ok(paymentService.mockPay(orderNo));
    }
}
