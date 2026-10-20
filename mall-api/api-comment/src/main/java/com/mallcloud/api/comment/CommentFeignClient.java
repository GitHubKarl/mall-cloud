package com.mallcloud.api.comment;

import com.mallcloud.api.comment.dto.CommentStatDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 评价服务内部接口。
 */
@FeignClient(name = "mall-comment", path = "/comment/inner", contextId = "commentFeignClient")
public interface CommentFeignClient {

    @GetMapping("/stat/{spuId}")
    R<CommentStatDTO> stat(@PathVariable("spuId") Long spuId);
}
