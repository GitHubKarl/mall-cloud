package com.mallcloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关启动类。
 * <p>
 * 扫描范围仅 {@code com.mallcloud.gateway} 与 {@code com.mallcloud.common.core}：
 * 网关是 WebFlux 应用，绝不能把 MVC 版公共组件扫进来。
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.mallcloud.gateway", "com.mallcloud.common.core"})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
