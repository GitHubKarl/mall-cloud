package com.mallcloud.api.search;

import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 搜索服务内部接口。索引写入以 MQ 为主，本接口只提供运维用的全量重建与健康检查。
 */
@FeignClient(name = "mall-search", path = "/search/inner", contextId = "searchFeignClient")
public interface SearchFeignClient {

    /** 全量重建索引（后台运维触发，异步执行，返回已提交任务数） */
    @PostMapping("/rebuild")
    R<Long> rebuildAll();

    /** 当前索引文档数，用于对账 */
    @GetMapping("/count")
    R<Long> count();
}
