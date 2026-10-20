package com.mallcloud.notice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.mallcloud.notice.mapper")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class NoticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeApplication.class, args);
    }
}
