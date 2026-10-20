package com.mallcloud.admin.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 后台首页概览。由 BFF 聚合多个服务的接口拼装，前端只需一次请求。
 */
public record DashboardVO(long indexDocCount,
                          String environment,
                          List<ServiceCheck> services) implements Serializable {

    /**
     * 单个依赖服务的健康状态。
     */
    public record ServiceCheck(String name, boolean available, String message) {
    }
}
