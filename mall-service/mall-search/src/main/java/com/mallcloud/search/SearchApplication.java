package com.mallcloud.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * 搜索服务。数据全部在 ES，没有关系库。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.api")
@EnableElasticsearchRepositories(basePackages = "com.mallcloud.search.repository")
@SpringBootApplication(scanBasePackages = "com.mallcloud")
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
