package com.mallcloud.notice.controller;

import com.mallcloud.api.notice.dto.NoticeDTO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import com.mallcloud.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知接口。
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 内部接口：强实时发送（如验证码） */
    @PostMapping("/inner/send")
    public R<Boolean> send(@RequestBody NoticeDTO dto) {
        return R.ok(noticeService.send(dto));
    }

    /** 我的站内信 */
    @RequiresLogin
    @GetMapping("/mine")
    public R<List<NoticeService.NoticeLogView>> mine(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return R.ok(noticeService.listSiteNotice(UserContext.requireUserId(), limit));
    }
}
