package com.mallcloud.user.controller;

import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.user.dto.UserDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部接口：仅允许服务间 Feign 调用，网关拒绝 /inner/** 外部访问。
 */
@RestController
@RequestMapping("/user/inner")
@RequiredArgsConstructor
public class UserInnerController {

    private final UserService userService;

    @GetMapping("/detail/{userId}")
    public R<UserDTO> getUser(@PathVariable("userId") Long userId) {
        return R.ok(userService.getUser(userId));
    }

    @GetMapping("/address/{addressId}")
    public R<AddressDTO> getAddress(@PathVariable("addressId") Long addressId) {
        return R.ok(userService.getAddress(addressId));
    }

    @GetMapping("/address/list/{userId}")
    public R<List<AddressDTO>> listAddress(@PathVariable("userId") Long userId) {
        return R.ok(userService.listAddress(userId));
    }
}
