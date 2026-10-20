package com.mallcloud.api.notice.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 通知发送入参。
 *
 * @param channel 渠道：SITE / SMS / EMAIL
 */
public record NoticeDTO(Long userId,
                        String channel,
                        String template,
                        Map<String, Object> params) implements Serializable {
}
