package com.mallcloud.api.coupon;

import com.mallcloud.api.coupon.dto.CouponUseDTO;
import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 优惠券服务内部接口。lock / release 随订单事务一起参与 Seata 全局事务。
 */
@FeignClient(name = "mall-coupon", path = "/coupon/inner", contextId = "couponFeignClient")
public interface CouponFeignClient {

    /** 锁定并核销优惠券，返回抵扣金额 */
    @PostMapping("/lock")
    R<CouponUseResultDTO> lock(@RequestBody CouponUseDTO dto);

    /** 订单关闭时退回优惠券（幂等，按 orderNo 去重） */
    @PostMapping("/release")
    R<Boolean> release(@RequestParam("orderNo") String orderNo);
}
