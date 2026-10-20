package com.mallcloud.api.user;

import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.user.dto.UserDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 会员服务内部接口。路径统一以 /inner 开头，网关对该前缀做拒绝转发。
 */
@FeignClient(name = "mall-user", path = "/user/inner", contextId = "userFeignClient")
public interface UserFeignClient {

    @GetMapping("/detail/{userId}")
    R<UserDTO> getUser(@PathVariable("userId") Long userId);

    @GetMapping("/address/{addressId}")
    R<AddressDTO> getAddress(@PathVariable("addressId") Long addressId);

    @GetMapping("/address/list/{userId}")
    R<List<AddressDTO>> listAddress(@PathVariable("userId") Long userId);
}
