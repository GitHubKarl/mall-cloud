package com.mallcloud.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品保存入参（SPU + SKU 一次性提交）。
 */
@Data
public class ProductSaveDTO implements Serializable {

    /** 为空表示新增，非空表示编辑 */
    private Long id;

    @NotNull(message = "类目不能为空")
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;

    private String mainImage;

    private String detail;

    @Valid
    @NotEmpty(message = "至少需要一个 SKU")
    private List<SkuItem> skus;

    @Data
    public static class SkuItem implements Serializable {

        private Long id;

        @NotBlank(message = "SKU 名称不能为空")
        private String skuName;

        private String skuCode;

        @NotNull(message = "SKU 价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        private BigDecimal price;

        private String image;

        private String attrs;
    }
}
