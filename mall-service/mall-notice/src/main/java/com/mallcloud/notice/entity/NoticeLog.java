package com.mallcloud.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送流水。成功失败都记录，便于排查"用户说没收到"的问题。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notice_log")
public class NoticeLog extends BaseEntity {

    private Long userId;

    private String channel;

    private String template;

    private String title;

    private String content;

    /** 0-待发送 1-成功 2-失败 */
    private Integer status;

    private String errorMsg;
}
