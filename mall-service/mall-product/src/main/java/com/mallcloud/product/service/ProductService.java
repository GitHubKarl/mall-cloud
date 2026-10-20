package com.mallcloud.product.service;

import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.product.dto.ProductQuery;
import com.mallcloud.product.dto.ProductSaveDTO;
import com.mallcloud.product.vo.ProductDetailVO;
import com.mallcloud.product.vo.ProductListVO;

import java.util.List;

/**
 * 商品服务。
 */
public interface ProductService {

    PageResult<ProductListVO> page(ProductQuery query);

    ProductDetailVO detail(Long spuId);

    Long save(ProductSaveDTO dto);

    void changeStatus(Long spuId, Integer status);

    SkuDTO getSku(Long skuId);

    List<SkuDTO> listSku(List<Long> skuIds);

    ProductDocDTO getDoc(Long spuId);

    /** 全量 SPU 主键，供搜索索引重建任务分页拉取 */
    List<Long> listAllSpuIds();
}
