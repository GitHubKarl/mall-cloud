package com.mallcloud.cart.repository;

import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车 Redis 仓储。
 * <p>
 * 存储结构：{@code mall:cart:{userId}} 为 Hash，field = skuId，value = CartItemRecord 的 JSON。
 * 用 Hash 而不是 String，是为了单个商品增删改不必读写整车数据。
 * <p>
 * 这里刻意用 StringRedisTemplate + 手工 JSON，而不是 RedisTemplate&lt;String,Object&gt;：
 * 后者带类型信息，实体类包名一改就反序列化失败，购物车这种热数据不值得冒这个险。
 */
@Repository
@RequiredArgsConstructor
public class CartRepository {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofDays(CommonConstants.CACHE_CART_TTL_DAYS);

    public String key(Long userId) {
        return CommonConstants.CACHE_CART + userId;
    }

    public void put(Long userId, CartItemRecord record) {
        String key = key(userId);
        redisTemplate.opsForHash().put(key, String.valueOf(record.skuId()), JsonUtils.toJson(record));
        redisTemplate.expire(key, TTL);
    }

    public void remove(Long userId, Long skuId) {
        redisTemplate.opsForHash().delete(key(userId), String.valueOf(skuId));
    }

    public void clear(Long userId) {
        redisTemplate.delete(key(userId));
    }

    /** 清空勾选行（下单成功后调用） */
    public int clearChecked(Long userId) {
        List<CartItemRecord> checked = list(userId).stream()
                .filter(item -> Boolean.TRUE.equals(item.checked()))
                .toList();
        if (checked.isEmpty()) {
            return 0;
        }
        Object[] fields = checked.stream().map(item -> String.valueOf(item.skuId())).toArray();
        Long removed = redisTemplate.opsForHash().delete(key(userId), fields);
        return removed == null ? 0 : removed.intValue();
    }

    public List<CartItemRecord> list(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key(userId));
        List<CartItemRecord> result = new ArrayList<>(entries.size());
        for (Object value : entries.values()) {
            CartItemRecord record = JsonUtils.parse(String.valueOf(value), CartItemRecord.class);
            if (record != null) {
                result.add(record);
            }
        }
        result.sort((a, b) -> Long.compare(b.skuId(), a.skuId()));
        return result;
    }

    public CartItemRecord get(Long userId, Long skuId) {
        Object value = redisTemplate.opsForHash().get(key(userId), String.valueOf(skuId));
        return value == null ? null : JsonUtils.parse(String.valueOf(value), CartItemRecord.class);
    }

    public long count(Long userId) {
        Long size = redisTemplate.opsForHash().size(key(userId));
        return size == null ? 0L : size;
    }
}
