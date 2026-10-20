package com.mallcloud.comment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mallcloud.api.comment.dto.CommentStatDTO;
import com.mallcloud.api.order.OrderFeignClient;
import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.api.product.ProductFeignClient;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.comment.dto.CommentCreateDTO;
import com.mallcloud.comment.entity.Comment;
import com.mallcloud.comment.mapper.CommentMapper;
import com.mallcloud.comment.service.CommentService;
import com.mallcloud.comment.vo.CommentVO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评价服务实现。
 * <p>
 * 评价资格由订单服务判定（订单已完成且属于本人），评价服务只负责自己的数据，
 * 不复制订单状态逻辑——这是微服务边界的基本纪律。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final OrderFeignClient orderFeignClient;
    private final ProductFeignClient productFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CommentCreateDTO dto) {
        Boolean canComment = orderFeignClient.canComment(dto.getOrderNo(), userId).getDataOrThrow();
        if (!Boolean.TRUE.equals(canComment)) {
            throw new BizException(ResultCode.COMMENT_NOT_ALLOWED);
        }

        Long exists = commentMapper.selectCount(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getOrderNo, dto.getOrderNo())
                .eq(Comment::getSkuId, dto.getSkuId()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.COMMENT_ALREADY_EXISTS);
        }

        OrderDTO order = orderFeignClient.getByOrderNo(dto.getOrderNo()).getDataOrThrow();
        // spuId 必须由商品服务给出，不能让前端传（前端可以随便传一个别的商品ID）
        SkuDTO sku = productFeignClient.getSku(dto.getSkuId()).getDataOrThrow();
        if (sku == null) {
            throw new BizException(ResultCode.PRODUCT_SKU_NOT_FOUND);
        }

        com.mallcloud.comment.entity.Comment comment = new com.mallcloud.comment.entity.Comment();
        comment.setUserId(userId);
        comment.setOrderNo(dto.getOrderNo());
        comment.setSkuId(dto.getSkuId());
        comment.setSpuId(sku.spuId());
        comment.setScore(dto.getScore());
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages() == null ? null : JsonUtils.toJson(dto.getImages()));
        comment.setAnonymous(Boolean.TRUE.equals(dto.getAnonymous()) ? 1 : 0);
        // 默认直接通过；若需要人工审核，改为 0 并由后台审核
        comment.setStatus(1);
        commentMapper.insert(comment);
        log.info("新增评价 orderNo={} skuId={} userId={} orderStatus={}",
                dto.getOrderNo(), dto.getSkuId(), userId, order == null ? null : order.status());
        return comment.getId();
    }

    @Override
    public PageResult<CommentVO> pageBySpu(Long spuId, PageQuery query) {
        Page<Comment> page = commentMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getSpuId, spuId)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getId));
        List<CommentVO> records = page.getRecords().stream()
                .map(comment -> new CommentVO(comment.getId(),
                        comment.getAnonymous() != null && comment.getAnonymous() == 1
                                ? "匿名用户" : "用户" + comment.getUserId(),
                        comment.getScore(), comment.getContent(),
                        comment.getImages() == null ? List.of() : JsonUtils.parseList(comment.getImages(), String.class),
                        comment.getReply(), comment.getCreateTime()))
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public CommentStatDTO stat(Long spuId) {
        Map<String, Object> row = commentMapper.statBySpuId(spuId);
        if (row == null) {
            return new CommentStatDTO(spuId, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long total = toLong(row.get("total"));
        BigDecimal avgScore = toDecimal(row.get("avg_score"));
        BigDecimal goodRate = toDecimal(row.get("good_rate"));
        return new CommentStatDTO(spuId, total, avgScore, goodRate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long commentId, String reply) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评价不存在");
        }
        Comment update = new Comment();
        update.setId(commentId);
        update.setReply(reply);
        update.setReplyTime(LocalDateTime.now());
        commentMapper.updateById(update);
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
