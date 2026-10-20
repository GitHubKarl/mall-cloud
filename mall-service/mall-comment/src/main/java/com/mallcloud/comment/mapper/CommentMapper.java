package com.mallcloud.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 商品评价统计。用一次聚合查询代替三次 count，减少数据库往返。
     */
    @Select("""
            SELECT COUNT(*)                                          AS total,
                   IFNULL(ROUND(AVG(score), 1), 0)                   AS avg_score,
                   IFNULL(ROUND(SUM(CASE WHEN score >= 4 THEN 1 ELSE 0 END) * 100 / COUNT(*), 1), 0) AS good_rate
              FROM t_comment
             WHERE spu_id = #{spuId}
               AND status = 1
               AND deleted = 0
            """)
    Map<String, Object> statBySpuId(@Param("spuId") Long spuId);
}
