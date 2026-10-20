package com.mallcloud.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 监控中心启动类。
 * <p>
 * 通过 Nacos 自动发现各服务的 actuator 端点，无需逐个配置实例地址。
 * 生产环境务必加 Spring Security 登录，否则任何人都能看到全部实例信息。
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }
}
