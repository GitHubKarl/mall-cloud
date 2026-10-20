package com.mallcloud.user.convert;

import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.user.dto.UserDTO;
import com.mallcloud.user.entity.User;
import com.mallcloud.user.entity.UserAddress;

/**
 * 实体与传输对象转换。不使用 MapStruct 以保持零注解处理器依赖，逻辑简单且可断点调试。
 */
public final class UserConvert {

    private UserConvert() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(),
                user.getAvatar(), user.getStatus(), user.getMemberLevel());
    }

    public static AddressDTO toDTO(UserAddress address) {
        if (address == null) {
            return null;
        }
        return new AddressDTO(address.getId(), address.getUserId(), address.getReceiver(), address.getPhone(),
                address.getProvince(), address.getCity(), address.getDistrict(), address.getDetail(),
                address.getPostalCode());
    }
}
