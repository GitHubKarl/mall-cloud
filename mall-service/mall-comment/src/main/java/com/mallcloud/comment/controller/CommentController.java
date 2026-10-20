package com.mallcloud.comment.controller;

import com.mallcloud.comment.dto.CommentCreateDTO;
import com.mallcloud.comment.service.CommentService;
import com.mallcloud.comment.vo.CommentVO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价前台接口。
 */
@Tag(name = "商品评价")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "商品评价列表")
    @GetMapping("/public/page/{spuId}")
    public R<PageResult<CommentVO>> page(@PathVariable("spuId") Long spuId, @Valid PageQuery query) {
        return R.ok(commentService.pageBySpu(spuId, query));
    }

    @Operation(summary = "发表评价")
    @RequiresLogin
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody CommentCreateDTO dto) {
        return R.ok(commentService.create(UserContext.requireUserId(), dto));
    }
}
