package com.mallcloud.admin.controller;

import com.mallcloud.admin.service.AdminService;
import com.mallcloud.admin.vo.DashboardVO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页。BFF 聚合多服务数据。
 */
@Tag(name = "后台首页")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;

    @Operation(summary = "概览")
    @RequiresLogin
    @GetMapping("/overview")
    public R<DashboardVO> overview() {
        return R.ok(adminService.dashboard());
    }
}
