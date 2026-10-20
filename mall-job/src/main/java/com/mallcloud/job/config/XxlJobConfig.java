package com.mallcloud.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job 执行器配置。调度中心地址走配置中心，避免打镜像时写死。
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobConfig {

    private String adminAddresses;
    private String accessToken;
    private String appName = "mall-job";
    private String address;
    private String ip;
    private int port = 9999;
    private String logPath = "./logs/xxl-job";
    private int logRetentionDays = 30;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("初始化 XXL-Job 执行器 appName={} admin={}", appName, adminAddresses);
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAccessToken(accessToken);
        executor.setAppname(appName);
        executor.setAddress(address);
        executor.setIp(ip);
        executor.setPort(port);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        return executor;
    }
}
