package com.mallcloud.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表项。
 */
public record OrderVO(String orderNo,
                      Integer status,
                      String statusDesc,
                      BigDecimal totalAmount,
                      BigDecimal payAmount,
                      LocalDateTime createTime,
                      LocalDateTime expireTime) implements Serializable {
}
