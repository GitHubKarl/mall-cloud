package com.mallcloud.notice.service;

import com.mallcloud.api.common.event.NoticeSendEvent;
import com.mallcloud.api.notice.dto.NoticeDTO;

import java.util.List;

/**
 * 通知服务。
 */
public interface NoticeService {

    /** 立即发送（强实时场景） */
    boolean send(NoticeDTO dto);

    /** 事件驱动发送（绝大多数场景） */
    boolean send(NoticeSendEvent event);

    /** 我的站内信 */
    List<NoticeLogView> listSiteNotice(Long userId, int limit);

    /** 站内信视图 */
    record NoticeLogView(Long id, String title, String content, java.time.LocalDateTime createTime) {
    }
}
