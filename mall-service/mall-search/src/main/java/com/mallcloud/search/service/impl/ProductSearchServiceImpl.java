package com.mallcloud.search.service.impl;

import com.mallcloud.api.product.ProductFeignClient;
import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.search.document.ProductDocument;
import com.mallcloud.search.dto.SearchQuery;
import com.mallcloud.search.service.ProductSearchService;
import com.mallcloud.search.vo.SearchItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 商品搜索实现。
 * <p>
 * 用 {@link CriteriaQuery} 而非原生 JSON DSL：字段改名时编译器能报错，
 * 也不需要手写容易出错的多层 bool 嵌套。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ProductFeignClient productFeignClient;

    private static final String INDEX_NAME = "mall_product";
    private static final IndexCoordinates INDEX = IndexCoordinates.of(INDEX_NAME);
    private static final Set<String> SORTABLE = Set.of("sales", "price", "updateTime");

    @Override
    public PageResult<SearchItemVO> search(SearchQuery query) {
        Criteria criteria = new Criteria("status").is(1);

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            criteria = criteria.and(new Criteria("name").matches(query.getKeyword()))
                    .or(new Criteria("subtitle").matches(query.getKeyword()));
        }
        if (query.getCategoryId() != null) {
            criteria = criteria.and(new Criteria("categoryId").is(String.valueOf(query.getCategoryId())));
        }
        if (query.getBrandId() != null) {
            criteria = criteria.and(new Criteria("brandId").is(String.valueOf(query.getBrandId())));
        }
        if (query.getMinPrice() != null || query.getMaxPrice() != null) {
            Criteria priceCriteria = new Criteria("price");
            if (query.getMinPrice() != null) {
                priceCriteria = priceCriteria.greaterThanEqual(query.getMinPrice());
            }
            if (query.getMaxPrice() != null) {
                priceCriteria = priceCriteria.lessThanEqual(query.getMaxPrice());
            }
            criteria = criteria.and(priceCriteria);
        }

        String sortField = SORTABLE.contains(query.getSortField()) ? query.getSortField() : "sales";
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria,
                PageRequest.of((int) Math.max(query.getPageNum() - 1, 0), (int) query.getPageSize()));
        criteriaQuery.addSort(query.isAsc()
                ? Sort.by(Sort.Direction.ASC, sortField)
                : Sort.by(Sort.Direction.DESC, sortField));

        try {
            SearchHits<ProductDocument> hits = operations.search(criteriaQuery, ProductDocument.class);
            List<SearchItemVO> records = hits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .map(doc -> new SearchItemVO(doc.getSpuId(), doc.getName(), doc.getSubtitle(),
                            doc.getPrice(), doc.getMainImage(), doc.getSales(), doc.getUpdateTime()))
                    .toList();
            return PageResult.of(hits.getTotalHits(), query.getPageNum(), query.getPageSize(), records);
        } catch (Exception e) {
            log.error("ES 查询失败 keyword={}", query.getKeyword(), e);
            throw new BizException(ResultCode.SEARCH_INDEX_ERROR);
        }
    }

    @Override
    public void upsert(ProductDocDTO doc, Long version) {
        if (doc == null || doc.spuId() == null) {
            return;
        }
        // 下架/删除的商品直接移除索引，避免脏数据被搜到
        if (doc.status() == null || doc.status() != 1) {
            delete(doc.spuId());
            return;
        }

        ProductDocument exists = operations.get(String.valueOf(doc.spuId()), ProductDocument.class, INDEX);
        if (exists != null && exists.getVersion() != null && version != null
                && version < exists.getVersion()) {
            log.info("索引版本较新，丢弃旧消息 spuId={} incoming={} exists={}",
                    doc.spuId(), version, exists.getVersion());
            return;
        }

        ProductDocument document = new ProductDocument();
        document.setSpuId(doc.spuId());
        document.setName(doc.name());
        document.setSubtitle(doc.subtitle());
        document.setCategoryId(doc.categoryId() == null ? null : String.valueOf(doc.categoryId()));
        document.setBrandId(doc.brandId() == null ? null : String.valueOf(doc.brandId()));
        document.setPrice(doc.price() == null ? null : doc.price().doubleValue());
        document.setMainImage(doc.mainImage());
        document.setSales(doc.sales());
        document.setStatus(doc.status());
        document.setVersion(version == null ? System.currentTimeMillis() : version);
        document.setUpdateTime(doc.updateTime());

        operations.save(document, INDEX);
        log.info("商品索引已更新 spuId={} version={}", doc.spuId(), document.getVersion());
    }

    @Override
    public void delete(Long spuId) {
        if (spuId == null) {
            return;
        }
        operations.delete(String.valueOf(spuId), INDEX);
        log.info("商品索引已删除 spuId={}", spuId);
    }

    @Override
    public long count() {
        try {
            return operations.count(Query.findAll(), ProductDocument.class);
        } catch (Exception e) {
            log.warn("统计索引文档数失败", e);
            return 0L;
        }
    }

    @Override
    public long rebuildAll() {
        List<Long> spuIds = productFeignClient.listAllSpuIds().getDataOrThrow();
        if (spuIds == null || spuIds.isEmpty()) {
            return 0L;
        }
        long version = System.currentTimeMillis();
        long success = 0;
        for (Long spuId : spuIds) {
            try {
                ProductDocDTO doc = productFeignClient.getProductDoc(spuId).getDataOrThrow();
                upsert(doc, version);
                success++;
            } catch (Exception e) {
                log.error("重建索引失败 spuId={}", spuId, e);
            }
        }
        log.info("索引重建完成 total={} success={}", spuIds.size(), success);
        return success;
    }
}
