package com.mallcloud.order.convert;

import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.order.constant.OrderStatus;
import com.mallcloud.order.entity.Order;
import com.mallcloud.order.entity.OrderItem;
import com.mallcloud.order.vo.OrderDetailVO;
import com.mallcloud.order.vo.OrderVO;

import java.util.List;

/**
 * 订单对象转换。
 */
public final class OrderConvert {

    private OrderConvert() {
    }

    public static OrderVO toVO(Order order) {
        return new OrderVO(order.getOrderNo(), order.getStatus(), OrderStatus.desc(order.getStatus()),
                order.getTotalAmount(), order.getPayAmount(), order.getCreateTime(), order.getExpireTime());
    }

    public static OrderDetailVO toDetailVO(Order order, List<OrderItem> items) {
        List<OrderDetailVO.Item> itemVOS = items.stream()
                .map(item -> new OrderDetailVO.Item(item.getSkuId(), item.getSkuName(), item.getSkuImage(),
                        item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderDetailVO(order.getOrderNo(), order.getStatus(), OrderStatus.desc(order.getStatus()),
                order.getTotalAmount(), order.getDiscountAmount(), order.getPayAmount(),
                order.getReceiver(), order.getReceiverPhone(), order.getReceiverAddress(),
                order.getRemark(), order.getCloseReason(), itemVOS);
    }

    public static OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDTO(order.getOrderNo(), order.getUserId(), order.getStatus(),
                OrderStatus.desc(order.getStatus()), order.getTotalAmount(), order.getPayAmount(),
                order.getCreateTime(), order.getPayTime());
    }
}
