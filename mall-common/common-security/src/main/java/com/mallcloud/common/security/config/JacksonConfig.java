package com.mallcloud.common.security.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 JSON 序列化约定。
 * <ul>
 *     <li>Long / long 输出为字符串：JS Number 只有 53 位精度，雪花 ID 直出会精度丢失</li>
 *     <li>时间统一 yyyy-MM-dd HH:mm:ss，前端不再各自处理时区</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        };
    }
}
