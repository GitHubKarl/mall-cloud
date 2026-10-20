package com.mallcloud.common.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;

import java.util.List;

/**
 * JSON 工具。不依赖 Spring 容器，网关与普通工具类均可直接使用。
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 序列化失败");
        }
    }

    public static byte[] toBytes(Object obj) {
        if (obj == null) {
            return new byte[0];
        }
        try {
            return MAPPER.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 序列化失败");
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 反序列化失败");
        }
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return MAPPER.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 反序列化失败");
        }
    }

    public static <T> T parse(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 反序列化失败");
        }
    }

    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 反序列化失败");
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "JSON 解析失败");
        }
    }
}
