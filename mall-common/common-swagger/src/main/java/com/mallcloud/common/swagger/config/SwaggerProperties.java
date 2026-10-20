package com.mallcloud.common.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文档配置项，各服务在 yml 中覆盖 title 即可。
 */
@Data
@ConfigurationProperties(prefix = "mall.swagger")
public class SwaggerProperties {

    private boolean enabled = true;
    private String title = "mall-cloud 接口文档";
    private String description = "B2C 电商微服务 API";
    private String version = "1.0.0";
    private String contactName = "mall-cloud";
    private String contactEmail = "dev@mallcloud.com";
}
