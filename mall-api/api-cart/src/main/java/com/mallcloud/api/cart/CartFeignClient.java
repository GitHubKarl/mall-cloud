package com.mallcloud.api.cart;

import com.mallcloud.api.cart.dto.CartItemDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 购物车服务内部接口。下单时订单服务拉取勾选行并清空。
 */
@FeignClient(name = "mall-cart", path = "/cart/inner", contextId = "cartFeignClient")
public interface CartFeignClient {

    @GetMapping("/checked/{userId}")
    R<List<CartItemDTO>> listChecked(@PathVariable("userId") Long userId);

    @PostMapping("/clear/{userId}")
    R<Boolean> clearChecked(@PathVariable("userId") Long userId);
}
