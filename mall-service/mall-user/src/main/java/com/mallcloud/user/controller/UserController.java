package com.mallcloud.user.controller;

import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import com.mallcloud.user.service.UserService;
import com.mallcloud.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员中心接口。
 */
@Tag(name = "会员中心")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前登录人信息")
    @RequiresLogin
    @GetMapping("/profile")
    public R<UserInfoVO> profile() {
        return R.ok(userService.getProfile(UserContext.requireUserId()));
    }

    @Operation(summary = "我的收货地址")
    @RequiresLogin
    @GetMapping("/address/list")
    public R<List<AddressDTO>> addressList() {
        return R.ok(userService.listAddress(UserContext.requireUserId()));
    }
}
