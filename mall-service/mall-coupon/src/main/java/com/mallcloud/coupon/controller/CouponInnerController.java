package com.mallcloud.coupon.controller;

import com.mallcloud.api.coupon.dto.CouponUseDTO;
import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口：order 服务下单时核销、关单时退回。
 */
@RestController
@RequestMapping("/coupon/inner")
@RequiredArgsConstructor
public class CouponInnerController {

    private final CouponService couponService;

    @PostMapping("/lock")
    public R<CouponUseResultDTO> lock(@RequestBody CouponUseDTO dto) {
        return R.ok(couponService.lock(dto));
    }

    @PostMapping("/release")
    public R<Boolean> release(@RequestParam("orderNo") String orderNo) {
        return R.ok(couponService.release(orderNo));
    }
}
