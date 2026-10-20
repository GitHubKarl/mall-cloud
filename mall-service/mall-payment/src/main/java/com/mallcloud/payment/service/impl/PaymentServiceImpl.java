package com.mallcloud.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.api.common.event.PaymentResultEvent;
import com.mallcloud.api.payment.dto.CreatePaymentDTO;
import com.mallcloud.api.payment.dto.PaymentDTO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.payment.entity.Payment;
import com.mallcloud.payment.mapper.PaymentMapper;
import com.mallcloud.payment.mq.PaymentEventProducer;
import com.mallcloud.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 支付服务实现。
 * <p>
 * 状态机：0-待支付 →(渠道回调)→ 1-支付成功 / 2-支付失败 →(退款)→ 3-已退款。
 * 所有状态迁移都用 CAS UPDATE，天然抵御渠道的重复回调。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentEventProducer eventProducer;

    private static final int STATUS_UNPAID = 0;
    private static final int STATUS_PAID = 1;
    private static final int STATUS_FAILED = 2;

    private static final Set<String> SUPPORTED_CHANNELS = Set.of("MOCK", "WECHAT_NATIVE", "ALIPAY");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO create(CreatePaymentDTO dto) {
        if (dto.orderNo() == null || dto.amount() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "订单号与金额不能为空");
        }
        String channel = dto.channel() == null ? "MOCK" : dto.channel().toUpperCase();
        if (!SUPPORTED_CHANNELS.contains(channel)) {
            throw new BizException(ResultCode.PAYMENT_CHANNEL_UNSUPPORTED);
        }

        // 同一订单复用未支付的支付单，避免用户反复点"去支付"生成一堆垃圾数据
        Payment exists = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, dto.orderNo())
                .eq(Payment::getStatus, STATUS_UNPAID)
                .orderByDesc(Payment::getId)
                .last("limit 1"));
        if (exists != null) {
            return toDTO(exists);
        }

        Payment payment = new Payment();
        payment.setPaymentNo("P" + IdUtil.getSnowflakeNextIdStr());
        payment.setOrderNo(dto.orderNo());
        payment.setUserId(dto.userId());
        payment.setAmount(dto.amount());
        payment.setChannel(channel);
        payment.setStatus(STATUS_UNPAID);
        paymentMapper.insert(payment);
        return toDTO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean callback(String channel, Map<String, String> params) {
        String paymentNo = params.get("paymentNo");
        String tradeNo = params.get("tradeNo");
        boolean success = "SUCCESS".equalsIgnoreCase(params.get("result"));

        if (paymentNo == null) {
            log.warn("回调缺少 paymentNo channel={} params={}", channel, params);
            return false;
        }
        // 生产环境：此处必须做渠道验签（微信 V3 证书验签 / 支付宝 RSA2 验签），验签失败直接拒绝
        log.info("收到支付回调 channel={} paymentNo={} success={}", channel, paymentNo, success);

        int toStatus = success ? STATUS_PAID : STATUS_FAILED;
        int rows = paymentMapper.casStatus(paymentNo, STATUS_UNPAID, toStatus,
                tradeNo == null ? "" : tradeNo);
        if (rows == 0) {
            // 已处理过：返回 true 让渠道停止重试（幂等）
            log.info("支付单状态已是终态，忽略重复回调 paymentNo={}", paymentNo);
            return true;
        }

        Payment payment = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getPaymentNo, paymentNo).last("limit 1"));
        if (payment == null) {
            return false;
        }

        eventProducer.publishResult(new PaymentResultEvent(payment.getPaymentNo(), payment.getOrderNo(),
                payment.getUserId(), success, tradeNo, payment.getAmount(), Instant.now()));
        return true;
    }

    @Override
    public PaymentDTO getByOrderNo(String orderNo) {
        Payment payment = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getId)
                .last("limit 1"));
        return payment == null ? null : toDTO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO mockPay(String orderNo) {
        Payment payment = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getId)
                .last("limit 1"));
        if (payment == null) {
            throw new BizException(ResultCode.PAYMENT_NOT_FOUND);
        }
        if (payment.getStatus() != null && payment.getStatus() == STATUS_PAID) {
            throw new BizException(ResultCode.PAYMENT_ALREADY_PAID);
        }

        String tradeNo = "MOCK" + IdUtil.getSnowflakeNextIdStr();
        int rows = paymentMapper.casStatus(payment.getPaymentNo(), STATUS_UNPAID, STATUS_PAID, tradeNo);
        if (rows == 0) {
            return toDTO(paymentMapper.selectById(payment.getId()));
        }
        payment.setStatus(STATUS_PAID);
        payment.setTradeNo(tradeNo);
        payment.setPayTime(LocalDateTime.now());

        eventProducer.publishResult(new PaymentResultEvent(payment.getPaymentNo(), payment.getOrderNo(),
                payment.getUserId(), true, tradeNo, payment.getAmount(), Instant.now()));
        return toDTO(payment);
    }

    private PaymentDTO toDTO(Payment payment) {
        String payUrl = payment.getChannel() != null && payment.getChannel().startsWith("MOCK")
                ? "/payment/mock/" + payment.getOrderNo()
                : null;
        return new PaymentDTO(payment.getPaymentNo(), payment.getOrderNo(), payment.getAmount(),
                payment.getStatus(), payment.getChannel(), payUrl);
    }
}
