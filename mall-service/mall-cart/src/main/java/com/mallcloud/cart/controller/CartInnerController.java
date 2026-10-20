package com.mallcloud.cart.controller;

import com.mallcloud.api.cart.dto.CartItemDTO;
import com.mallcloud.cart.service.CartService;
import com.mallcloud.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部接口：order 服务下单时读取勾选行并清空。
 */
@RestController
@RequestMapping("/cart/inner")
@RequiredArgsConstructor
public class CartInnerController {

    private final CartService cartService;

    @GetMapping("/checked/{userId}")
    public R<List<CartItemDTO>> listChecked(@PathVariable("userId") Long userId) {
        return R.ok(cartService.checkedItems(userId));
    }

    @PostMapping("/clear/{userId}")
    public R<Boolean> clearChecked(@PathVariable("userId") Long userId) {
        cartService.clearChecked(userId);
        return R.ok(true);
    }
}
