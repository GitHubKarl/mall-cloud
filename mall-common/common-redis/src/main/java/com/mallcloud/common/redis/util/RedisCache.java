package com.mallcloud.common.redis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存门面。统一封装「缓存未命中则回源」的模板代码，防止各服务重复实现缓存穿透处理。
 */
@Component
@RequiredArgsConstructor
public class RedisCache {

    private final RedisTemplate<String, Object> redisTemplate;

    /* ---------------- String ---------------- */

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存回源模板：命中直接返回；未命中执行 loader 并写缓存。
     *
     * @param ttl 过期时间，传 null 表示不过期
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (type.isInstance(cached)) {
                return (T) cached;
            }
            return null;
        }
        T value = loader.get();
        if (value != null) {
            if (ttl == null) {
                set(key, value);
            } else {
                set(key, value, ttl);
            }
        }
        return value;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count == null ? 0L : count;
    }

    public long deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return delete(keys);
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire == null ? -2L : expire;
    }

    /* ---------------- Hash ---------------- */

    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void hSetAll(String key, Map<String, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public long hDelete(String key, Object... fields) {
        Long count = redisTemplate.opsForHash().delete(key, fields);
        return count == null ? 0L : count;
    }

    public boolean hExists(String key, String field) {
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, field));
    }

    /* ---------------- List / Set ---------------- */

    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end, Class<T> type) {
        List<Object> range = redisTemplate.opsForList().range(key, start, end);
        if (range == null) {
            return List.of();
        }
        return range.stream().filter(type::isInstance).map(type::cast).toList();
    }

    public void sAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> sMembers(String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return members == null ? Set.of() : members;
    }

    /* ---------------- 计数器 ---------------- */

    public long increment(String key, long delta) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        return value == null ? 0L : value;
    }

    public long decrement(String key, long delta) {
        Long value = redisTemplate.opsForValue().decrement(key, delta);
        return value == null ? 0L : value;
    }
}
