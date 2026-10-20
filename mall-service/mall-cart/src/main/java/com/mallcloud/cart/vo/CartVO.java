package com.mallcloud.cart.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 整车视图。totalAmount 只累计"勾选且有效"的行，与结算页金额保持一致。
 */
public record CartVO(List<CartItemVO> items,
                     int totalCount,
                     int checkedCount,
                     BigDecimal totalAmount) implements Serializable {
}
