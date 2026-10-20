package com.mallcloud.common.core.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页入参基类。所有列表查询 DTO 继承它即可，避免每个接口重复声明分页字段。
 */
@Data
@NoArgsConstructor
public class PageQuery implements Serializable {

    @Min(value = 1, message = "页码不能小于 1")
    private long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 200, message = "每页条数不能超过 200")
    private long pageSize = 10L;

    /** MyBatis-Plus offset，仅 Service 层使用 */
    public long offset() {
        return (Math.max(pageNum, 1) - 1) * Math.max(pageSize, 1);
    }
}
