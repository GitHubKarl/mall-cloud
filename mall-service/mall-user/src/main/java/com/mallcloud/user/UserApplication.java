package com.mallcloud.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 会员服务启动类。
 * <p>
 * {@code scanBasePackages = "com.mallcloud"} 让 common-* 下的配置类（拦截器、异常处理、MP 插件…）
 * 一次扫描全部生效，无需在每个服务重复写 @Import。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.api")
@MapperScan("com.mallcloud.user.mapper")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
