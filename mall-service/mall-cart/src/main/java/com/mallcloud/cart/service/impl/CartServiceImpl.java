package com.mallcloud.cart.service.impl;

import com.mallcloud.api.cart.dto.CartItemDTO;
import com.mallcloud.api.product.ProductFeignClient;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.cart.dto.CartAddDTO;
import com.mallcloud.cart.repository.CartItemRecord;
import com.mallcloud.cart.repository.CartRepository;
import com.mallcloud.cart.service.CartService;
import com.mallcloud.cart.vo.CartItemVO;
import com.mallcloud.cart.vo.CartVO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。
 * <p>
 * 关键取舍：<b>不缓存价格</b>。每次查看都批量回源商品服务（一次 RPC 拿全部 SKU），
 * 换来的是"购物车价格永远与商品详情一致"，这比省一次 RPC 重要得多。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;

    @Override
    public void add(Long userId, CartAddDTO dto) {
        // 加购前先确认商品可售，避免脏数据进车
        SkuDTO sku = productFeignClient.getSku(dto.getSkuId()).getDataOrThrow();
        if (sku == null) {
            throw new BizException(ResultCode.PRODUCT_SKU_NOT_FOUND);
        }

        CartItemRecord exists = cartRepository.get(userId, dto.getSkuId());
        int quantity = exists == null ? dto.getQuantity() : exists.quantity() + dto.getQuantity();
        cartRepository.put(userId, new CartItemRecord(dto.getSkuId(), quantity, true));
    }

    @Override
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "数量必须大于 0");
        }
        CartItemRecord exists = cartRepository.get(userId, skuId);
        if (exists == null) {
            throw new BizException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        cartRepository.put(userId, new CartItemRecord(skuId, quantity, exists.checked()));
    }

    @Override
    public void changeChecked(Long userId, Long skuId, Boolean checked) {
        CartItemRecord exists = cartRepository.get(userId, skuId);
        if (exists == null) {
            throw new BizException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        cartRepository.put(userId, new CartItemRecord(skuId, exists.quantity(), checked));
    }

    @Override
    public void changeAllChecked(Long userId, Boolean checked) {
        List<CartItemRecord> items = cartRepository.list(userId);
        if (items.isEmpty()) {
            return;
        }
        items.forEach(item -> cartRepository.put(userId,
                new CartItemRecord(item.skuId(), item.quantity(), checked)));
    }

    @Override
    public void remove(Long userId, Long skuId) {
        cartRepository.remove(userId, skuId);
    }

    @Override
    public void clear(Long userId) {
        cartRepository.clear(userId);
    }

    @Override
    public CartVO view(Long userId) {
        List<CartItemRecord> records = cartRepository.list(userId);
        if (records.isEmpty()) {
            return new CartVO(List.of(), 0, 0, BigDecimal.ZERO);
        }

        Map<Long, SkuDTO> skuMap = loadSkus(records.stream().map(CartItemRecord::skuId).toList());

        List<CartItemVO> items = new ArrayList<>(records.size());
        int totalCount = 0;
        int checkedCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemRecord record : records) {
            SkuDTO sku = skuMap.get(record.skuId());
            boolean valid = sku != null;
            BigDecimal price = valid ? sku.price() : BigDecimal.ZERO;
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(record.quantity()));

            boolean checked = Boolean.TRUE.equals(record.checked());
            totalCount += record.quantity();
            if (valid && checked) {
                checkedCount += record.quantity();
                totalAmount = totalAmount.add(subtotal);
            }

            items.add(new CartItemVO(record.skuId(),
                    valid ? sku.skuName() : "商品已下架",
                    valid ? sku.image() : null,
                    price, record.quantity(), record.checked(), subtotal, valid));
        }
        return new CartVO(items, totalCount, checkedCount, totalAmount);
    }

    @Override
    public List<CartItemDTO> checkedItems(Long userId) {
        List<CartItemRecord> records = cartRepository.list(userId).stream()
                .filter(item -> Boolean.TRUE.equals(item.checked()))
                .toList();
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, SkuDTO> skuMap = loadSkus(records.stream().map(CartItemRecord::skuId).toList());
        return records.stream()
                .filter(record -> skuMap.containsKey(record.skuId()))
                .map(record -> new CartItemDTO(record.skuId(), record.quantity(), true,
                        skuMap.get(record.skuId()).price()))
                .toList();
    }

    @Override
    public int clearChecked(Long userId) {
        return cartRepository.clearChecked(userId);
    }

    private Map<Long, SkuDTO> loadSkus(List<Long> skuIds) {
        try {
            List<SkuDTO> skus = productFeignClient.listSku(skuIds).getDataOrThrow();
            return skus.stream().collect(Collectors.toMap(SkuDTO::skuId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            // 商品服务不可用时降级：整车标记失效，而不是让页面 500
            log.warn("批量查询 SKU 失败，本次按失效商品展示", e);
            return Map.of();
        }
    }
}
