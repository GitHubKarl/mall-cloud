package com.mallcloud.common.core.result;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应体。total 为总条数，pages 为总页数。
 */
public record PageResult<T>(long total, long pageNum, long pageSize, long pages, List<T> records)
        implements Serializable {

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(0L, pageNum, pageSize, 0L, Collections.emptyList());
    }

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> records) {
        long pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        return new PageResult<>(total, pageNum, pageSize, pages, records);
    }
}
