package com.mallcloud.user.controller;

import com.mallcloud.common.core.result.R;
import com.mallcloud.user.dto.LoginDTO;
import com.mallcloud.user.dto.RegisterDTO;
import com.mallcloud.user.service.UserService;
import com.mallcloud.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（网关白名单放行）。
 */
@Tag(name = "会员认证")
@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<Long> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<LoginVO> refresh(@RequestParam("refreshToken") String refreshToken) {
        return R.ok(userService.refresh(refreshToken));
    }
}
