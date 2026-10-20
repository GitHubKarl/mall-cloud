package com.mallcloud.comment.controller;

import com.mallcloud.api.comment.dto.CommentStatDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口：商品详情页展示评价统计。
 */
@RestController
@RequestMapping("/comment/inner")
@RequiredArgsConstructor
public class CommentInnerController {

    private final CommentService commentService;

    @GetMapping("/stat/{spuId}")
    public R<CommentStatDTO> stat(@PathVariable("spuId") Long spuId) {
        return R.ok(commentService.stat(spuId));
    }
}
