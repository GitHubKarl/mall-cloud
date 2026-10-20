package com.mallcloud.api.user.dto;

import java.io.Serializable;

/**
 * 收货地址。订单服务下单时通过 Feign 拉取并快照到订单表，避免地址变更影响历史订单。
 */
public record AddressDTO(Long id,
                         Long userId,
                         String receiver,
                         String phone,
                         String province,
                         String city,
                         String district,
                         String detail,
                         String postalCode) implements Serializable {

    public String fullAddress() {
        return province + city + district + detail;
    }
}
