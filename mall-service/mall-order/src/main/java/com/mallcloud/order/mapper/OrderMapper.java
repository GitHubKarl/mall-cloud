package com.mallcloud.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 状态机推进：只有当前状态符合预期才更新，天然防止并发重复支付 / 重复关单。
     *
     * @return 影响行数，0 表示状态已变更，调用方应视为"已被处理"
     */
    @Update("""
            UPDATE t_order
               SET status = #{toStatus}, update_time = NOW()
             WHERE order_no = #{orderNo}
               AND status = #{fromStatus}
               AND deleted = 0
            """)
    int casStatus(@Param("orderNo") String orderNo,
                  @Param("fromStatus") Integer fromStatus,
                  @Param("toStatus") Integer toStatus);
}
