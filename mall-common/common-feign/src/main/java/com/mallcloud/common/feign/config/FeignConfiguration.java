package com.mallcloud.common.feign.config;

import com.mallcloud.common.feign.decoder.InnerErrorDecoder;
import com.mallcloud.common.feign.interceptor.FeignRequestInterceptor;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 全局配置。
 * <p>
 * 注意：<b>不要</b>在本类上加 {@code @FeignClient(configuration = ...)} 引用，
 * 否则会为每个客户端创建子容器导致配置重复；本项目采用"全局生效"策略。
 */
@Configuration(proxyBeanMethods = false)
public class FeignConfiguration {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new InnerErrorDecoder();
    }

    /**
     * 关闭 Feign 默认重试：写操作重试会放大重复下单风险，重试统一交给 Sentinel / 业务补偿。
     */
    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
