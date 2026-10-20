package com.mallcloud.cart.service;

import com.mallcloud.api.cart.dto.CartItemDTO;
import com.mallcloud.cart.dto.CartAddDTO;
import com.mallcloud.cart.vo.CartVO;

import java.util.List;

/**
 * 购物车服务。
 */
public interface CartService {

    void add(Long userId, CartAddDTO dto);

    void updateQuantity(Long userId, Long skuId, Integer quantity);

    void changeChecked(Long userId, Long skuId, Boolean checked);

    void changeAllChecked(Long userId, Boolean checked);

    void remove(Long userId, Long skuId);

    void clear(Long userId);

    CartVO view(Long userId);

    List<CartItemDTO> checkedItems(Long userId);

    int clearChecked(Long userId);
}
