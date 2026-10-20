package com.mallcloud.search.repository;

import com.mallcloud.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * ES 仓储。常规增删改查走 Spring Data 的派生方法，
 * 复杂查询（多条件筛选 + 分页）由 Service 层的 ElasticsearchOperations 完成。
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByCategoryId(String categoryId);
}
