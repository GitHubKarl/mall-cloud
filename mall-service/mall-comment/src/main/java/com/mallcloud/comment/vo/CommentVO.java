package com.mallcloud.comment.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价展示项。匿名评价隐藏用户昵称。
 */
public record CommentVO(Long id,
                        String nickname,
                        Integer score,
                        String content,
                        List<String> images,
                        String reply,
                        LocalDateTime createTime) implements Serializable {
}
