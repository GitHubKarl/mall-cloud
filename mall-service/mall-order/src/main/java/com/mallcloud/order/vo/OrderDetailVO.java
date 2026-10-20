package com.mallcloud.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情。
 */
public record OrderDetailVO(String orderNo,
                            Integer status,
                            String statusDesc,
                            BigDecimal totalAmount,
                            BigDecimal discountAmount,
                            BigDecimal payAmount,
                            String receiver,
                            String receiverPhone,
                            String receiverAddress,
                            String remark,
                            String closeReason,
                            List<Item> items) implements Serializable {

    public record Item(Long skuId, String skuName, String skuImage, BigDecimal price, Integer quantity) {
    }
}
