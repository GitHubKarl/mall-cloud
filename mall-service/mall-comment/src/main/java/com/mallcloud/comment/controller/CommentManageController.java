package com.mallcloud.comment.controller;

import com.mallcloud.comment.service.CommentService;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价管理接口（后台）。
 */
@Tag(name = "评价管理")
@RestController
@RequestMapping("/comment/manage")
@RequiredArgsConstructor
public class CommentManageController {

    private final CommentService commentService;

    @Operation(summary = "商家回复")
    @RequiresPerm("comment:reply")
    @PostMapping("/reply/{commentId}")
    public R<Void> reply(@PathVariable("commentId") Long commentId,
                         @RequestParam("reply") String reply) {
        commentService.reply(commentId, reply);
        return R.ok();
    }
}
