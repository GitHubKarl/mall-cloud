package com.mallcloud.common.core.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 自动装配。被各服务的 {@code scanBasePackages = "com.mallcloud"} 扫描到即可生效，
 * 不需要 SPI 文件，避免组件扫描与自动装配重复注册同一 Bean。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    @Bean
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties);
    }
}
