package com.mallcloud.order.service;

import com.mallcloud.api.common.event.PaymentResultEvent;
import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.order.dto.OrderCreateDTO;
import com.mallcloud.order.vo.OrderCreateVO;
import com.mallcloud.order.vo.OrderDetailVO;
import com.mallcloud.order.vo.OrderVO;

/**
 * 订单服务。
 */
public interface OrderService {

    /** 下单：Seata 全局事务覆盖「锁券 + 扣库存 + 落单」 */
    OrderCreateVO create(OrderCreateDTO dto);

    /** 用户主动取消 */
    void cancel(String orderNo);

    OrderDetailVO detail(String orderNo);

    PageResult<OrderVO> pageMine(Integer status, PageQuery query);

    /** 支付结果回调处理（由 MQ 消费者调用，幂等） */
    void handlePaymentResult(PaymentResultEvent event);

    /** 超时关单（由延迟消息或 mall-job 扫描触发，幂等） */
    void closeTimeout(String orderNo);

    boolean canComment(String orderNo, Long userId);

    /**
     * 批量关单兜底：扫描已超时的待支付订单并关闭。
     * <p>
     * 延迟消息是主链路，本方法是保险丝——MQ 丢消息、Broker 重启都可能让订单永远挂着。
     *
     * @return 本次实际关闭的订单数
     */
    int closeExpired(int limit);

    OrderDTO getByOrderNo(String orderNo);

    /** 发货（后台） */
    void ship(String orderNo);

    /** 确认收货 */
    void finish(String orderNo);
}
