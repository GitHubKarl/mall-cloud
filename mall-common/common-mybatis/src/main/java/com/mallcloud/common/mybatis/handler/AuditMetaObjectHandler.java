package com.mallcloud.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充。插入时写入 createTime/updateTime/deleted，更新时只刷新 updateTime。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String DELETED = "deleted";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
        strictInsertFill(metaObject, DELETED, Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
