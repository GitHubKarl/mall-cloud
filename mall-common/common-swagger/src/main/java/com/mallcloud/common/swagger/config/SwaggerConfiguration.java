package com.mallcloud.common.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 统一配置。访问地址：http://ip:port/doc.html
 * <p>
 * 通过 {@code mall.swagger.enabled=false} 可在生产环境整体关闭文档。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "mall.swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI mallOpenAPI(SwaggerProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(properties.getContactEmail())))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name(SECURITY_SCHEME_NAME)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
