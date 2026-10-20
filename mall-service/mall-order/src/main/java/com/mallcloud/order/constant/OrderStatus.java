package com.mallcloud.order.constant;

import java.util.Set;

/**
 * 订单状态常量与合法流转表。
 * <p>
 * 把状态机显式写成代码，而不是散落在各处的 if：新增状态时只需改这里 + 数据库枚举。
 */
public final class OrderStatus {

    /** 待支付 */
    public static final int UNPAID = 0;
    /** 已支付待发货 */
    public static final int PAID = 1;
    /** 已发货 */
    public static final int SHIPPED = 2;
    /** 已完成 */
    public static final int FINISHED = 3;
    /** 已取消（用户主动） */
    public static final int CANCELED = 4;
    /** 已关闭（超时/支付失败） */
    public static final int CLOSED = 5;

    /** 支付超时分钟数 */
    public static final long PAY_TIMEOUT_MINUTES = 30L;

    private OrderStatus() {
    }

    public static String desc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case UNPAID -> "待支付";
            case PAID -> "待发货";
            case SHIPPED -> "已发货";
            case FINISHED -> "已完成";
            case CANCELED -> "已取消";
            case CLOSED -> "已关闭";
            default -> "未知";
        };
    }

    /** 不可再变更的终态 */
    public static boolean isFinal(Integer status) {
        return status != null && Set.of(FINISHED, CANCELED, CLOSED).contains(status);
    }

    /**
     * 校验流转合法性，非法直接抛业务异常。
     */
    public static void assertTransition(Integer from, Integer to) {
        boolean ok = switch (from == null ? -1 : from) {
            case UNPAID -> to == PAID || to == CANCELED || to == CLOSED;
            case PAID -> to == SHIPPED || to == CLOSED;
            case SHIPPED -> to == FINISHED;
            default -> false;
        };
        if (!ok) {
            throw new com.mallcloud.common.core.exception.BizException(
                    com.mallcloud.common.core.result.ResultCode.ORDER_STATUS_ERROR);
        }
    }
}
