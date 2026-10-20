package com.mallcloud.api.comment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品评价统计。商品详情页展示用。
 *
 * @param total    评价总数
 * @param avgScore 平均分（保留 1 位小数）
 * @param goodRate 好评率（0~100）
 */
public record CommentStatDTO(Long spuId, Long total, BigDecimal avgScore, BigDecimal goodRate)
        implements Serializable {
}
