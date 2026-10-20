package com.mallcloud.comment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发表评价。
 */
@Data
public class CommentCreateDTO implements Serializable {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer score;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private List<String> images;

    private Boolean anonymous = false;
}
