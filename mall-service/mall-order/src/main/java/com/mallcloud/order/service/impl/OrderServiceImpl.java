package com.mallcloud.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mallcloud.api.cart.CartFeignClient;
import com.mallcloud.api.cart.dto.CartItemDTO;
import com.mallcloud.api.common.event.OrderClosedEvent;
import com.mallcloud.api.common.event.OrderCreatedEvent;
import com.mallcloud.api.common.event.PaymentResultEvent;
import com.mallcloud.api.coupon.CouponFeignClient;
import com.mallcloud.api.coupon.dto.CouponUseDTO;
import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.api.product.ProductFeignClient;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.api.user.UserFeignClient;
import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.ware.WareFeignClient;
import com.mallcloud.api.ware.dto.StockItemDTO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.order.constant.OrderStatus;
import com.mallcloud.order.convert.OrderConvert;
import com.mallcloud.order.dto.OrderCreateDTO;
import com.mallcloud.order.entity.Order;
import com.mallcloud.order.entity.OrderItem;
import com.mallcloud.order.mapper.OrderItemMapper;
import com.mallcloud.order.mapper.OrderMapper;
import com.mallcloud.order.mq.OrderEventProducer;
import com.mallcloud.order.service.OrderService;
import com.mallcloud.order.vo.OrderCreateVO;
import com.mallcloud.order.vo.OrderDetailVO;
import com.mallcloud.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 * <p>
 * <b>事务边界（重要）</b>：
 * <ul>
 *     <li>Seata AT 只包住下单方法：锁券 + 扣库存 + 写订单，是必须强一致的唯一链路</li>
 *     <li>清购物车、发消息、发通知全部放在事务提交之后，失败不回滚订单</li>
 *     <li>支付结果、超时关单通过 CAS 更新状态，天然幂等，可安全重复消费</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderEventProducer eventProducer;

    private final CartFeignClient cartFeignClient;
    private final ProductFeignClient productFeignClient;
    private final WareFeignClient wareFeignClient;
    private final CouponFeignClient couponFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @GlobalTransactional(name = "mall-create-order", rollbackFor = Exception.class, timeoutMills = 30000)
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO create(OrderCreateDTO dto) {
        Long userId = UserContext.requireUserId();

        // 1. 取购物车勾选项（价格一律以商品服务为准，不信任前端）
        List<CartItemDTO> cartItems = cartFeignClient.listChecked(userId).getDataOrThrow();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BizException(ResultCode.CART_EMPTY);
        }

        List<Long> skuIds = cartItems.stream().map(CartItemDTO::skuId).toList();
        Map<Long, SkuDTO> skuMap = productFeignClient.listSku(skuIds).getDataOrThrow().stream()
                .collect(Collectors.toMap(SkuDTO::skuId, Function.identity(), (a, b) -> a));

        // 2. 地址快照
        AddressDTO address = userFeignClient.getAddress(dto.getAddressId()).getDataOrThrow();
        if (address == null || !userId.equals(address.userId())) {
            throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
        }

        // 3. 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDTO cartItem : cartItems) {
            SkuDTO sku = skuMap.get(cartItem.skuId());
            if (sku == null) {
                throw new BizException(ResultCode.PRODUCT_SKU_NOT_FOUND);
            }
            totalAmount = totalAmount.add(sku.price().multiply(BigDecimal.valueOf(cartItem.quantity())));
        }

        // 4. 锁券（Seata 参与者）。orderNo 先于锁券生成，作为券的幂等键，关单时据此退回
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (dto.getCouponId() != null) {
            CouponUseResultDTO couponResult = couponFeignClient
                    .lock(new CouponUseDTO(userId, dto.getCouponId(), orderNo, totalAmount))
                    .getDataOrThrow();
            if (couponResult != null && couponResult.discountAmount() != null) {
                discountAmount = couponResult.discountAmount();
            }
        }
        BigDecimal payAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        // 5. 预扣库存（Seata 参与者）
        List<StockItemDTO> stockItems = cartItems.stream()
                .map(item -> new StockItemDTO(item.skuId(), item.quantity()))
                .toList();
        wareFeignClient.lock(stockItems, orderNo).getDataOrThrow();

        // 6. 落单
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(OrderStatus.PAY_TIMEOUT_MINUTES);
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatus.UNPAID);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setCouponId(dto.getCouponId());
        order.setReceiver(address.receiver());
        order.setReceiverPhone(address.phone());
        order.setReceiverAddress(address.fullAddress());
        order.setRemark(dto.getRemark());
        order.setExpireTime(expireTime);
        orderMapper.insert(order);

        for (CartItemDTO cartItem : cartItems) {
            SkuDTO sku = skuMap.get(cartItem.skuId());
            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setSpuId(sku.spuId());
            item.setSkuId(sku.skuId());
            item.setSkuName(sku.skuName());
            item.setSkuImage(sku.image());
            item.setPrice(sku.price());
            item.setQuantity(cartItem.quantity());
            item.setTotalAmount(sku.price().multiply(BigDecimal.valueOf(cartItem.quantity())));
            orderItemMapper.insert(item);
        }

        // 7. 事务提交后的副作用
        afterCommit(() -> {
            try {
                cartFeignClient.clearChecked(userId);
            } catch (Exception e) {
                log.warn("清空购物车失败，不影响下单 userId={}", userId, e);
            }
            eventProducer.sendDelayClose(orderNo, OrderStatus.PAY_TIMEOUT_MINUTES * 60 * 1000);
            eventProducer.publishCreated(new OrderCreatedEvent(orderNo, userId, payAmount,
                    cartItems.stream()
                            .map(ci -> new OrderCreatedEvent.OrderItemPayload(ci.skuId(),
                                    skuMap.get(ci.skuId()).skuName(), ci.quantity(),
                                    skuMap.get(ci.skuId()).price()))
                            .toList(),
                    Instant.now()));
        });

        return new OrderCreateVO(orderNo, totalAmount, discountAmount, payAmount, expireTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo) {
        Order order = requireOrder(orderNo);
        if (!order.getUserId().equals(UserContext.requireUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        close(order, OrderStatus.CANCELED, "CANCEL");
    }

    @Override
    public OrderDetailVO detail(String orderNo) {
        Order order = requireOrder(orderNo);
        List<OrderItem> items = listItems(orderNo);
        return OrderConvert.toDetailVO(order, items);
    }

    @Override
    public PageResult<OrderVO> pageMine(Integer status, PageQuery query) {
        Long userId = UserContext.requireUserId();
        Page<Order> page = orderMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, userId)
                        .eq(status != null, Order::getStatus, status)
                        .orderByDesc(Order::getId));
        List<OrderVO> records = page.getRecords().stream().map(OrderConvert::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentResult(PaymentResultEvent event) {
        if (event == null || event.orderNo() == null) {
            return;
        }
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, event.orderNo()).last("limit 1"));
        if (order == null) {
            log.warn("支付结果对应的订单不存在 orderNo={}", event.orderNo());
            return;
        }

        if (!event.success()) {
            log.warn("支付失败，关闭订单 orderNo={}", event.orderNo());
            close(order, OrderStatus.CLOSED, "PAY_FAILED");
            return;
        }

        int rows = orderMapper.casStatus(event.orderNo(), OrderStatus.UNPAID, OrderStatus.PAID);
        if (rows == 0) {
            log.info("订单已处理，忽略重复支付结果 orderNo={}", event.orderNo());
            return;
        }
        Order update = new Order();
        update.setId(order.getId());
        update.setPayTime(LocalDateTime.now());
        orderMapper.updateById(update);

        try {
            wareFeignClient.deduct(event.orderNo()).getDataOrThrow();
        } catch (Exception e) {
            // 库存实扣失败不阻塞订单状态，交由 mall-job 的库存对账任务补偿
            log.error("支付成功后库存实扣失败，转人工对账 orderNo={}", event.orderNo(), e);
        }
        log.info("订单支付成功 orderNo={} amount={}", event.orderNo(), event.amount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeout(String orderNo) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo).last("limit 1"));
        if (order == null) {
            return;
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.UNPAID) {
            return;
        }
        close(order, OrderStatus.CLOSED, "TIMEOUT");
    }

    @Override
    public boolean canComment(String orderNo, Long userId) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo).last("limit 1"));
        return order != null
                && order.getUserId() != null && order.getUserId().equals(userId)
                && order.getStatus() != null && order.getStatus() == OrderStatus.FINISHED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeExpired(int limit) {
        List<Order> expired = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getStatus, OrderStatus.UNPAID)
                .lt(Order::getExpireTime, LocalDateTime.now())
                .orderByAsc(Order::getId)
                .last("limit " + Math.max(limit, 1)));
        if (expired.isEmpty()) {
            return 0;
        }
        int closed = 0;
        for (Order order : expired) {
            try {
                close(order, OrderStatus.CLOSED, "TIMEOUT");
                closed++;
            } catch (Exception e) {
                log.error("兜底关单失败 orderNo={}", order.getOrderNo(), e);
            }
        }
        log.info("兜底关单完成 scanned={} closed={}", expired.size(), closed);
        return closed;
    }

    @Override
    public OrderDTO getByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo).last("limit 1"));
        return OrderConvert.toDTO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(String orderNo) {
        int rows = orderMapper.casStatus(orderNo, OrderStatus.PAID, OrderStatus.SHIPPED);
        if (rows == 0) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
        Order update = new Order();
        update.setShipTime(LocalDateTime.now());
        orderMapper.update(update, Wrappers.<Order>lambdaUpdate().eq(Order::getOrderNo, orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(String orderNo) {
        Order order = requireOrder(orderNo);
        if (!order.getUserId().equals(UserContext.requireUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        int rows = orderMapper.casStatus(orderNo, OrderStatus.SHIPPED, OrderStatus.FINISHED);
        if (rows == 0) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
        Order update = new Order();
        update.setFinishTime(LocalDateTime.now());
        orderMapper.update(update, Wrappers.<Order>lambdaUpdate().eq(Order::getOrderNo, orderNo));
    }

    /* ---------------- 内部方法 ---------------- */

    /** 统一关单：CAS 更新 + 提交后广播关闭事件（库存解锁、券退回、通知） */
    private void close(Order order, int targetStatus, String reason) {
        OrderStatus.assertTransition(order.getStatus(), targetStatus);
        int rows = orderMapper.casStatus(order.getOrderNo(), order.getStatus(), targetStatus);
        if (rows == 0) {
            log.info("订单状态已变更，忽略本次关单 orderNo={}", order.getOrderNo());
            return;
        }

        Order update = new Order();
        update.setCloseTime(LocalDateTime.now());
        update.setCloseReason(reason);
        orderMapper.update(update, Wrappers.<Order>lambdaUpdate().eq(Order::getOrderNo, order.getOrderNo()));

        List<OrderClosedEvent.StockItemPayload> payloads = listItems(order.getOrderNo()).stream()
                .map(item -> new OrderClosedEvent.StockItemPayload(item.getSkuId(), item.getQuantity()))
                .toList();

        afterCommit(() -> eventProducer.publishClosed(new OrderClosedEvent(order.getOrderNo(),
                order.getUserId(), reason, payloads, order.getCouponId(), Instant.now())));
    }

    private Order requireOrder(String orderNo) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo).last("limit 1"));
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private List<OrderItem> listItems(String orderNo) {
        return orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderNo, orderNo));
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
