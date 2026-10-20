package com.mallcloud.common.core.security;

import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * JWT 工具。仅依赖 jjwt + JDK，网关与业务服务共用。
 * <p>
 * Token 载荷约定：sub=username，uid=用户主键，typ=用户类型，rol=角色，prm=权限，tk=令牌类型。
 */
public class JwtUtils {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_USER_TYPE = "typ";
    public static final String CLAIM_ROLES = "rol";
    public static final String CLAIM_PERMS = "prm";
    public static final String CLAIM_TOKEN_TYPE = "tk";

    public static final String TOKEN_ACCESS = "access";
    public static final String TOKEN_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 签发访问令牌 */
    public String createAccessToken(LoginUser user) {
        return build(user, TOKEN_ACCESS, properties.getAccessTokenExpire());
    }

    /** 签发刷新令牌 */
    public String createRefreshToken(LoginUser user) {
        return build(user, TOKEN_REFRESH, properties.getRefreshTokenExpire());
    }

    private String build(LoginUser user, String tokenType, long expireSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.username())
                .claim(CLAIM_USER_ID, user.userId())
                .claim(CLAIM_USER_TYPE, user.userType())
                .claim(CLAIM_ROLES, user.roles() == null ? Set.of() : user.roles())
                .claim(CLAIM_PERMS, user.perms() == null ? Set.of() : user.perms())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(secretKey)
                .compact();
    }

    /** 解析访问令牌，失败返回 null（由调用方决定抛 401 还是放行） */
    public LoginUser parseAccess(String token) {
        return doParse(token, TOKEN_ACCESS);
    }

    /** 解析刷新令牌，失败返回 null */
    public LoginUser parseRefresh(String token) {
        return doParse(token, TOKEN_REFRESH);
    }

    private LoginUser doParse(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                return null;
            }
            Number userIdClaim = claims.get(CLAIM_USER_ID, Number.class);
            Number userTypeClaim = claims.get(CLAIM_USER_TYPE, Number.class);

            return new LoginUser(userIdClaim == null ? null : userIdClaim.longValue(),
                    claims.getSubject(),
                    userTypeClaim == null ? null : userTypeClaim.intValue(),
                    toStringSet(claims.get(CLAIM_ROLES)),
                    toStringSet(claims.get(CLAIM_PERMS)));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** 解析访问令牌，失败直接抛 401 */
    public LoginUser parseOrThrow(String token) {
        LoginUser user = parseAccess(token);
        if (user == null || user.userId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    /** 剥离 Bearer 前缀 */
    public static String resolveToken(String authorizationHeader, String bearerPrefix) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (authorizationHeader.startsWith(bearerPrefix)) {
            return authorizationHeader.substring(bearerPrefix.length()).trim();
        }
        return authorizationHeader.trim();
    }

    public long getAccessTokenExpire() {
        return properties.getAccessTokenExpire();
    }

    private Set<String> toStringSet(Object value) {
        if (value instanceof Iterable<?> iterable) {
            Set<String> result = new HashSet<>();
            iterable.forEach(item -> result.add(String.valueOf(item)));
            return result;
        }
        if (value instanceof String str && !str.isBlank()) {
            return new HashSet<>(Arrays.asList(str.split(",")));
        }
        return Set.of();
    }
}
