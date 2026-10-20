package com.mallcloud.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板。content 中用 {key} 占位，发送时用参数替换。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notice_template")
public class NoticeTemplate extends BaseEntity {

    /** 模板编码，如 ORDER_PAID */
    private String code;

    private String name;

    /** SITE / SMS / EMAIL */
    private String channel;

    private String title;

    private String content;

    private Integer status;
}
