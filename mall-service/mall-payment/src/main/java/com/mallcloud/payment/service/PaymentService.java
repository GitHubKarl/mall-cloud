package com.mallcloud.payment.service;

import com.mallcloud.api.payment.dto.CreatePaymentDTO;
import com.mallcloud.api.payment.dto.PaymentDTO;

import java.util.Map;

/**
 * 支付服务。
 */
public interface PaymentService {

    /** 创建支付单（同一订单复用未支付的支付单） */
    PaymentDTO create(CreatePaymentDTO dto);

    /**
     * 渠道异步回调。
     *
     * @param params 渠道原始参数（已解析为 Map）
     * @return true 表示处理成功，渠道应停止重试
     */
    boolean callback(String channel, Map<String, String> params);

    PaymentDTO getByOrderNo(String orderNo);

    /** 模拟支付（仅开发环境，生产必须删除或加环境开关） */
    PaymentDTO mockPay(String orderNo);
}
