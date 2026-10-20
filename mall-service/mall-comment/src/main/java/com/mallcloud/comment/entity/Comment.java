package com.mallcloud.comment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评价。一个订单明细只能评价一次（order_no + sku_id 唯一索引）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_comment")
public class Comment extends BaseEntity {

    private Long userId;

    private String orderNo;

    private Long spuId;

    private Long skuId;

    /** 1~5 星 */
    private Integer score;

    private String content;

    /** 图片地址 JSON 数组 */
    private String images;

    /** 是否匿名 1-是 */
    private Integer anonymous;

    /** 0-待审核 1-已通过 2-已拒绝 */
    private Integer status;

    private String reply;

    private java.time.LocalDateTime replyTime;
}
