package com.mallcloud.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 购物车服务。没有数据源，因此<b>不引入</b> common-mybatis，
 * 否则 MyBatis-Plus 自动配置会因缺少 DataSource 而启动失败。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.api")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
