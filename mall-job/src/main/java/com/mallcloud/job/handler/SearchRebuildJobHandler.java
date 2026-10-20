package com.mallcloud.job.handler;

import com.mallcloud.api.search.SearchFeignClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 商品索引全量重建任务。建议每天凌晨执行一次，作为增量同步的一致性兜底。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchRebuildJobHandler {

    private final SearchFeignClient searchFeignClient;

    @XxlJob("searchRebuildHandler")
    public void execute() {
        XxlJobHelper.log("开始全量重建商品索引");
        try {
            Long count = searchFeignClient.rebuildAll().getDataOrThrow();
            XxlJobHelper.handleSuccess("重建完成，写入文档数：" + count);
            log.info("索引重建完成 count={}", count);
        } catch (Exception e) {
            XxlJobHelper.handleFail("索引重建失败：" + e.getMessage());
            log.error("索引重建任务失败", e);
        }
    }
}
