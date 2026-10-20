package com.mallcloud.api.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单快照，供后台、评价、通知等场景读取。
 */
public record OrderDTO(String orderNo,
                       Long userId,
                       Integer status,
                       String statusDesc,
                       BigDecimal totalAmount,
                       BigDecimal payAmount,
                       LocalDateTime createTime,
                       LocalDateTime payTime) implements Serializable {
}
