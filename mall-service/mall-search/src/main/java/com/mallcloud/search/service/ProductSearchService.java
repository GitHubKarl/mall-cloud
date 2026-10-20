package com.mallcloud.search.service;

import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.search.dto.SearchQuery;
import com.mallcloud.search.vo.SearchItemVO;

/**
 * 商品搜索服务。
 */
public interface ProductSearchService {

    PageResult<SearchItemVO> search(SearchQuery query);

    /** 幂等 upsert：旧版本消息自动丢弃 */
    void upsert(ProductDocDTO doc, Long version);

    void delete(Long spuId);

    long count();

    /** 全量重建：先清空再逐条写入，耗时操作，由后台或定时任务触发 */
    long rebuildAll();
}
