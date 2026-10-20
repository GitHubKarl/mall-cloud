package com.mallcloud.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.admin.dto.AdminLoginDTO;
import com.mallcloud.admin.entity.AdminUser;
import com.mallcloud.admin.mapper.AdminUserMapper;
import com.mallcloud.admin.service.AdminService;
import com.mallcloud.admin.vo.AdminLoginVO;
import com.mallcloud.admin.vo.DashboardVO;
import com.mallcloud.api.search.SearchFeignClient;
import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 后台管理服务实现。
 * <p>
 * 这里的 dashboard 是典型的 BFF 价值点：前端要 5 个服务的数据，BFF 用一次请求聚合，
 * 并且对每个下游单独兜底——某个服务挂了只影响那一块，不会整页崩。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtils jwtUtils;
    private final SearchFeignClient searchFeignClient;

    /** 与 mall-user 使用同一套 BCrypt 参数，便于统一密码策略 */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {
        AdminUser admin = adminUserMapper.selectOne(Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, dto.getUsername())
                .last("limit 1"));
        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BizException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (admin.getStatus() != null && admin.getStatus() == CommonConstants.STATUS_DISABLED) {
            throw new BizException(ResultCode.USER_DISABLED);
        }

        LoginUser loginUser = toLoginUser(admin);
        AdminUser update = new AdminUser();
        update.setId(admin.getId());
        update.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(update);

        return new AdminLoginVO(jwtUtils.createAccessToken(loginUser),
                jwtUtils.createRefreshToken(loginUser),
                jwtUtils.getAccessTokenExpire(),
                admin.getId(), admin.getUsername(), admin.getNickname(),
                loginUser.roles(), loginUser.perms());
    }

    @Override
    public AdminLoginVO refresh(String refreshToken) {
        LoginUser parsed = jwtUtils.parseRefresh(refreshToken);
        if (parsed == null || parsed.userId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        AdminUser admin = adminUserMapper.selectById(parsed.userId());
        if (admin == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        LoginUser loginUser = toLoginUser(admin);
        return new AdminLoginVO(jwtUtils.createAccessToken(loginUser),
                jwtUtils.createRefreshToken(loginUser),
                jwtUtils.getAccessTokenExpire(),
                admin.getId(), admin.getUsername(), admin.getNickname(),
                loginUser.roles(), loginUser.perms());
    }

    @Override
    public DashboardVO dashboard() {
        List<DashboardVO.ServiceCheck> checks = new ArrayList<>();
        long indexCount = 0L;

        try {
            Long count = searchFeignClient.count().getDataOrThrow();
            indexCount = count == null ? 0L : count;
            checks.add(new DashboardVO.ServiceCheck("mall-search", true, "索引文档 " + indexCount));
        } catch (Exception e) {
            checks.add(new DashboardVO.ServiceCheck("mall-search", false, e.getMessage()));
        }

        return new DashboardVO(indexCount, "dev", checks);
    }

    private LoginUser toLoginUser(AdminUser admin) {
        Set<String> roles = new HashSet<>();
        roles.add(admin.getRoleCode() == null ? "OPERATOR" : admin.getRoleCode());

        Set<String> perms = new HashSet<>();
        if (admin.getPerms() != null && !admin.getPerms().isBlank()) {
            perms.addAll(Arrays.asList(admin.getPerms().split(",")));
        }
        return new LoginUser(admin.getId(), admin.getUsername(), LoginUser.TYPE_ADMIN, roles, perms);
    }
}
