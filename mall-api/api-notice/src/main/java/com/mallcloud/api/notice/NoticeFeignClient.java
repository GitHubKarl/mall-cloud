package com.mallcloud.api.notice;

import com.mallcloud.api.notice.dto.NoticeDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通知服务内部接口。优先走 MQ，本接口仅用于「必须立即知道是否送达」的强实时场景（如支付验证码）。
 */
@FeignClient(name = "mall-notice", path = "/notice/inner", contextId = "noticeFeignClient")
public interface NoticeFeignClient {

    @PostMapping("/send")
    R<Boolean> send(@RequestBody NoticeDTO dto);
}
