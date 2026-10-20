package com.mallcloud.admin.controller;

import com.mallcloud.admin.dto.AdminLoginDTO;
import com.mallcloud.admin.service.AdminService;
import com.mallcloud.admin.vo.AdminLoginVO;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台认证接口（网关白名单放行）。
 */
@Tag(name = "后台认证")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @Operation(summary = "后台登录")
    @PostMapping("/login")
    public R<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return R.ok(adminService.login(dto));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<AdminLoginVO> refresh(@RequestParam("refreshToken") String refreshToken) {
        return R.ok(adminService.refresh(refreshToken));
    }

    @Operation(summary = "当前登录管理员")
    @RequiresLogin
    @GetMapping("/profile")
    public R<LoginUser> profile() {
        return R.ok(UserContext.get());
    }
}
