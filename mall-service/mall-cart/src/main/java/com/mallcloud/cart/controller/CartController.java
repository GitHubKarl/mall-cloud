package com.mallcloud.cart.controller;

import com.mallcloud.cart.dto.CartAddDTO;
import com.mallcloud.cart.service.CartService;
import com.mallcloud.cart.vo.CartVO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车接口。全部需要登录。
 */
@Tag(name = "购物车")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "查看购物车")
    @RequiresLogin
    @GetMapping
    public R<CartVO> view() {
        return R.ok(cartService.view(UserContext.requireUserId()));
    }

    @Operation(summary = "加入购物车")
    @RequiresLogin
    @PostMapping("/add")
    public R<Void> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.add(UserContext.requireUserId(), dto);
        return R.ok();
    }

    @Operation(summary = "修改数量")
    @RequiresLogin
    @PutMapping("/quantity")
    public R<Void> updateQuantity(@RequestParam("skuId") Long skuId,
                                  @RequestParam("quantity") Integer quantity) {
        cartService.updateQuantity(UserContext.requireUserId(), skuId, quantity);
        return R.ok();
    }

    @Operation(summary = "勾选 / 取消勾选单个商品")
    @RequiresLogin
    @PutMapping("/checked")
    public R<Void> changeChecked(@RequestParam("skuId") Long skuId,
                                 @RequestParam("checked") Boolean checked) {
        cartService.changeChecked(UserContext.requireUserId(), skuId, checked);
        return R.ok();
    }

    @Operation(summary = "全选 / 全不选")
    @RequiresLogin
    @PutMapping("/checked/all")
    public R<Void> changeAllChecked(@RequestParam("checked") Boolean checked) {
        cartService.changeAllChecked(UserContext.requireUserId(), checked);
        return R.ok();
    }

    @Operation(summary = "移除商品")
    @RequiresLogin
    @DeleteMapping("/{skuId}")
    public R<Void> remove(@PathVariable("skuId") Long skuId) {
        cartService.remove(UserContext.requireUserId(), skuId);
        return R.ok();
    }

    @Operation(summary = "清空购物车")
    @RequiresLogin
    @DeleteMapping("/clear")
    public R<Void> clear() {
        cartService.clear(UserContext.requireUserId());
        return R.ok();
    }
}
