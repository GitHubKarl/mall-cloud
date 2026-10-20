package com.mallcloud.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.api")
@MapperScan("com.mallcloud.ware.mapper")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class WareApplication {

    public static void main(String[] args) {
        SpringApplication.run(WareApplication.class, args);
    }
}
