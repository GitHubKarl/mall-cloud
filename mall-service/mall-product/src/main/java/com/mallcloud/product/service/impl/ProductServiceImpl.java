package com.mallcloud.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mallcloud.api.common.event.ProductChangeType;
import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.redis.util.RedisCache;
import com.mallcloud.product.convert.ProductConvert;
import com.mallcloud.product.dto.ProductQuery;
import com.mallcloud.product.dto.ProductSaveDTO;
import com.mallcloud.product.entity.ProductSku;
import com.mallcloud.product.entity.ProductSpu;
import com.mallcloud.product.mapper.ProductSkuMapper;
import com.mallcloud.product.mapper.ProductSpuMapper;
import com.mallcloud.product.mq.ProductEventProducer;
import com.mallcloud.product.service.ProductService;
import com.mallcloud.product.vo.ProductDetailVO;
import com.mallcloud.product.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 * <p>
 * 缓存策略：详情读多写少，采用 Cache-Aside；写操作后删缓存（不更新缓存），
 * 避免并发写导致缓存与库不一致。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductEventProducer eventProducer;
    private final RedisCache redisCache;

    /** 允许的排序字段白名单，防止前端传任意列名造成注入 */
    private static final Set<String> SORTABLE = Set.of("sales", "price", "createTime", "id");

    @Override
    public PageResult<ProductListVO> page(ProductQuery query) {
        LambdaQueryWrapper<ProductSpu> wrapper = Wrappers.<ProductSpu>lambdaQuery()
                .eq(query.getStatus() != null, ProductSpu::getStatus, query.getStatus())
                .eq(query.getCategoryId() != null, ProductSpu::getCategoryId, query.getCategoryId())
                .eq(query.getBrandId() != null, ProductSpu::getBrandId, query.getBrandId())
                .ge(query.getMinPrice() != null, ProductSpu::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, ProductSpu::getPrice, query.getMaxPrice())
                .like(query.getKeyword() != null && !query.getKeyword().isBlank(),
                        ProductSpu::getName, query.getKeyword());

        String sortField = SORTABLE.contains(query.getSortField()) ? query.getSortField() : "createTime";
        boolean asc = query.isAsc();
        switch (sortField) {
            case "sales" -> wrapper.orderBy(true, asc, ProductSpu::getSales);
            case "price" -> wrapper.orderBy(true, asc, ProductSpu::getPrice);
            case "id" -> wrapper.orderBy(true, asc, ProductSpu::getId);
            default -> wrapper.orderBy(true, asc, ProductSpu::getCreateTime);
        }

        Page<ProductSpu> page = spuMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<ProductListVO> records = page.getRecords().stream().map(ProductConvert::toListVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public ProductDetailVO detail(Long spuId) {
        String cacheKey = CommonConstants.CACHE_PRODUCT + "detail:" + spuId;
        ProductDetailVO cached = redisCache.get(cacheKey, ProductDetailVO.class);
        if (cached != null) {
            return cached;
        }

        ProductSpu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        List<ProductSku> skus = skuMapper.selectList(Wrappers.<ProductSku>lambdaQuery()
                .eq(ProductSku::getSpuId, spuId)
                .eq(ProductSku::getStatus, CommonConstants.STATUS_ENABLED)
                .orderByAsc(ProductSku::getId));

        ProductDetailVO vo = ProductConvert.toDetailVO(spu, skus);
        redisCache.set(cacheKey, vo, Duration.ofMinutes(CommonConstants.CACHE_PRODUCT_TTL_MINUTES));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(ProductSaveDTO dto) {
        BigDecimal minPrice = dto.getSkus().stream()
                .map(ProductSaveDTO.SkuItem::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        ProductSpu spu = new ProductSpu();
        spu.setId(dto.getId());
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandId(dto.getBrandId());
        spu.setName(dto.getName());
        spu.setSubtitle(dto.getSubtitle());
        spu.setMainImage(dto.getMainImage());
        spu.setDetail(dto.getDetail());
        spu.setPrice(minPrice);
        spu.setSales(0);
        spu.setStatus(CommonConstants.STATUS_DISABLED);

        boolean isCreate = dto.getId() == null;
        if (isCreate) {
            spuMapper.insert(spu);
        } else {
            ProductSpu exists = spuMapper.selectById(dto.getId());
            if (exists == null) {
                throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
            }
            spu.setSales(exists.getSales());
            spuMapper.updateById(spu);
        }

        for (ProductSaveDTO.SkuItem item : dto.getSkus()) {
            ProductSku sku = new ProductSku();
            sku.setId(item.getId());
            sku.setSpuId(spu.getId());
            sku.setSkuName(item.getSkuName());
            sku.setSkuCode(item.getSkuCode());
            sku.setPrice(item.getPrice());
            sku.setImage(item.getImage());
            sku.setAttrs(item.getAttrs());
            sku.setStatus(CommonConstants.STATUS_ENABLED);
            if (item.getId() == null) {
                skuMapper.insert(sku);
            } else {
                skuMapper.updateById(sku);
            }
        }

        publishAfterCommit(spu.getId(), isCreate ? ProductChangeType.CREATE : ProductChangeType.UPDATE);
        evictCache(spu.getId());
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long spuId, Integer status) {
        ProductSpu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        ProductSpu update = new ProductSpu();
        update.setId(spuId);
        update.setStatus(status);
        spuMapper.updateById(update);

        publishAfterCommit(spuId, status != null && status == CommonConstants.STATUS_ENABLED
                ? ProductChangeType.ON_SHELF : ProductChangeType.OFF_SHELF);
        evictCache(spuId);
    }

    @Override
    public SkuDTO getSku(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(ResultCode.PRODUCT_SKU_NOT_FOUND);
        }
        ProductSpu spu = spuMapper.selectById(sku.getSpuId());
        if (spu == null || spu.getStatus() == null || spu.getStatus() != CommonConstants.STATUS_ENABLED) {
            throw new BizException(ResultCode.PRODUCT_OFF_SHELF);
        }
        return ProductConvert.toSkuDTO(sku, spu);
    }

    @Override
    public List<SkuDTO> listSku(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        List<ProductSku> skus = skuMapper.selectByIds(skuIds);
        if (skus.isEmpty()) {
            return List.of();
        }
        Set<Long> spuIds = skus.stream().map(ProductSku::getSpuId).collect(Collectors.toSet());
        Map<Long, ProductSpu> spuMap = spuMapper.selectByIds(spuIds).stream()
                .collect(Collectors.toMap(ProductSpu::getId, Function.identity()));
        return skus.stream().map(sku -> ProductConvert.toSkuDTO(sku, spuMap.get(sku.getSpuId()))).toList();
    }

    @Override
    public ProductDocDTO getDoc(Long spuId) {
        ProductSpu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return ProductConvert.toDocDTO(spu);
    }

    @Override
    public List<Long> listAllSpuIds() {
        return spuMapper.selectList(Wrappers.<ProductSpu>lambdaQuery().select(ProductSpu::getId))
                .stream().map(ProductSpu::getId).toList();
    }

    /** 事务提交后再发消息：回滚时不会留下脏索引 */
    private void publishAfterCommit(Long spuId, ProductChangeType type) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventProducer.publishChanged(spuId, type);
                }
            });
        } else {
            eventProducer.publishChanged(spuId, type);
        }
    }

    private void evictCache(Long spuId) {
        redisCache.delete(CommonConstants.CACHE_PRODUCT + "detail:" + spuId);
    }
}
