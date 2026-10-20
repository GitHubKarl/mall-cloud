package com.mallcloud.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 任务执行器启动类。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.api")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}
