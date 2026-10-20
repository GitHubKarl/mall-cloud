package com.mallcloud.common.core.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。所有服务共享同一份 secret（Nacos 公共配置下发），保证网关签发的 Token 各服务均可解析。
 */
@Data
@ConfigurationProperties(prefix = "mall.jwt")
public class JwtProperties {

    /** 签名密钥，HS256 要求至少 32 字节，生产环境必须走配置中心并定期轮换 */
    private String secret = "mall-cloud-default-secret-please-change-in-production-2026";

    /** 访问令牌有效期（秒），默认 2 小时 */
    private long accessTokenExpire = 7200L;

    /** 刷新令牌有效期（秒），默认 7 天 */
    private long refreshTokenExpire = 604800L;

    private String issuer = "mall-cloud";
}
