package com.mallcloud.coupon.controller;

import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import com.mallcloud.coupon.service.CouponService;
import com.mallcloud.coupon.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券前台接口。
 */
@Tag(name = "优惠券")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "我的可用优惠券")
    @RequiresLogin
    @GetMapping("/available")
    public R<List<UserCouponVO>> available() {
        return R.ok(couponService.listAvailable(UserContext.requireUserId()));
    }

    @Operation(summary = "我的优惠券（按状态）")
    @RequiresLogin
    @GetMapping("/mine")
    public R<List<UserCouponVO>> mine(@RequestParam(value = "status", required = false) Integer status) {
        return R.ok(couponService.listMine(UserContext.requireUserId(), status));
    }

    @Operation(summary = "领取优惠券")
    @RequiresLogin
    @PostMapping("/receive/{couponId}")
    public R<Long> receive(@PathVariable("couponId") Long couponId) {
        return R.ok(couponService.receive(UserContext.requireUserId(), couponId));
    }

    @Operation(summary = "结算页试算抵扣金额")
    @RequiresLogin
    @GetMapping("/calculate")
    public R<CouponUseResultDTO> calculate(@RequestParam("couponId") Long couponId,
                                           @RequestParam("orderAmount") BigDecimal orderAmount) {
        return R.ok(couponService.calculate(UserContext.requireUserId(), couponId, orderAmount));
    }
}
