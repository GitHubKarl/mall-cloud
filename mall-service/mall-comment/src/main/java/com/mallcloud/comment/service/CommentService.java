package com.mallcloud.comment.service;

import com.mallcloud.api.comment.dto.CommentStatDTO;
import com.mallcloud.comment.dto.CommentCreateDTO;
import com.mallcloud.comment.vo.CommentVO;
import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;

/**
 * 评价服务。
 */
public interface CommentService {

    Long create(Long userId, CommentCreateDTO dto);

    PageResult<CommentVO> pageBySpu(Long spuId, PageQuery query);

    CommentStatDTO stat(Long spuId);

    void reply(Long commentId, String reply);
}
