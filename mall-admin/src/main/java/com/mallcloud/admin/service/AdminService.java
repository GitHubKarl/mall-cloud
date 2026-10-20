package com.mallcloud.admin.service;

import com.mallcloud.admin.dto.AdminLoginDTO;
import com.mallcloud.admin.vo.AdminLoginVO;
import com.mallcloud.admin.vo.DashboardVO;

/**
 * 后台管理服务。
 */
public interface AdminService {

    AdminLoginVO login(AdminLoginDTO dto);

    AdminLoginVO refresh(String refreshToken);

    /** BFF 聚合：一次性拿到多服务概览 */
    DashboardVO dashboard();
}
