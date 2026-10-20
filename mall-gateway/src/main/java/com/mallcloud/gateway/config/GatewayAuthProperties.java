package com.mallcloud.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关白名单配置。支持 Ant 风格通配，服务上线新公开接口时优先改配置中心而不是改代码。
 */
@Data
@ConfigurationProperties(prefix = "mall.gateway")
public class GatewayAuthProperties {

    private boolean authEnabled = true;

    /** 无需登录即可访问的路径 */
    private List<String> whiteList = new ArrayList<>(List.of(
            "/user/auth/login",
            "/user/auth/register",
            "/user/auth/refresh",
            "/user/auth/captcha",
            "/product/public/**",
            "/search/public/**",
            "/comment/public/**",
            "/notice/public/**",
            "/admin/auth/login",
            "/actuator/**",
            "/doc.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/favicon.ico"
    ));

    /** 无论是否登录都禁止外部访问的路径（内部 Feign 接口） */
    private List<String> denyList = new ArrayList<>(List.of("/**/inner/**"));
}
