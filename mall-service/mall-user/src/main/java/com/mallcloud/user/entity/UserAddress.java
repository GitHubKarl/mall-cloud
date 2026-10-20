package com.mallcloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址。下单时快照到订单表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_address")
public class UserAddress extends BaseEntity {

    private Long userId;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detail;

    private String postalCode;

    /** 1-默认地址 */
    private Integer isDefault;
}
