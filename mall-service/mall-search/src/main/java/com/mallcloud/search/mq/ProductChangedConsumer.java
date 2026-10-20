package com.mallcloud.search.mq;

import com.mallcloud.api.common.event.ProductChangedEvent;
import com.mallcloud.api.product.ProductFeignClient;
import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 商品变更消费者：增量同步 ES。
 * <p>
 * 消息里只带 spuId，不带宽表数据——原因有二：
 * <ol>
 *     <li>商品字段随时会变，消费时回源才能取到最新值</li>
 *     <li>消息体小，MQ 压力低；商品服务已有单条查询与缓存</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductChangedConsumer {

    private final ProductSearchService searchService;
    private final ProductFeignClient productFeignClient;

    @RabbitListener(queues = MqConstants.QUEUE_PRODUCT_SEARCH_SYNC)
    public void onMessage(String payload) {
        ProductChangedEvent event = JsonUtils.parse(payload, ProductChangedEvent.class);
        if (event == null || event.spuId() == null) {
            log.warn("收到空的商品变更事件，丢弃: {}", payload);
            return;
        }

        if ("DELETE".equals(event.changeType()) || "OFF_SHELF".equals(event.changeType())) {
            searchService.delete(event.spuId());
            return;
        }

        try {
            ProductDocDTO doc = productFeignClient.getProductDoc(event.spuId()).getDataOrThrow();
            searchService.upsert(doc, event.version());
        } catch (Exception e) {
            // 抛出异常让 MQ 重试；超过重试次数进入死信队列，由 mall-job 兜底对账
            log.error("同步商品索引失败 spuId={}", event.spuId(), e);
            throw e;
        }
    }
}
