package com.mallcloud.api.common.event;

/**
 * 商品变更类型。
 */
public enum ProductChangeType {

    /** 新建：需要新增索引 */
    CREATE,

    /** 修改：需要 upsert 索引 */
    UPDATE,

    /** 删除：需要移除索引 */
    DELETE,

    /** 上架：索引可见 */
    ON_SHELF,

    /** 下架：索引移除或标记不可售 */
    OFF_SHELF
}
