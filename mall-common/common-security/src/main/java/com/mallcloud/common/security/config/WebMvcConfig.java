package com.mallcloud.common.security.config;

import com.mallcloud.common.security.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 横切配置：注册登录态拦截器。文档、健康检查、错误页全部放行。
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    private static final String[] EXCLUDE_PATHS = {
            "/actuator/**",
            "/error",
            "/favicon.ico",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(0);
    }
}
