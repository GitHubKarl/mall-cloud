package com.mallcloud.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.user.dto.UserDTO;
import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.security.JwtUtils;
import com.mallcloud.common.redis.util.RedisCache;
import com.mallcloud.user.convert.UserConvert;
import com.mallcloud.user.dto.LoginDTO;
import com.mallcloud.user.dto.RegisterDTO;
import com.mallcloud.user.entity.User;
import com.mallcloud.user.entity.UserAddress;
import com.mallcloud.user.mapper.UserAddressMapper;
import com.mallcloud.user.mapper.UserMapper;
import com.mallcloud.user.service.UserService;
import com.mallcloud.user.vo.LoginVO;
import com.mallcloud.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 会员服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .or()
                .eq(User::getPhone, dto.getPhone()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setStatus(CommonConstants.STATUS_ENABLED);
        user.setMemberLevel(1);
        user.setTotalAmount(0L);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .and(w -> w.eq(User::getUsername, dto.getAccount())
                        .or()
                        .eq(User::getPhone, dto.getAccount()))
                .last("limit 1"));

        if (user == null) {
            throw new BizException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstants.STATUS_DISABLED) {
            throw new BizException(ResultCode.USER_DISABLED);
        }

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), LoginUser.TYPE_MEMBER,
                Set.of("MEMBER"), Set.of());

        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);

        return new LoginVO(jwtUtils.createAccessToken(loginUser),
                jwtUtils.createRefreshToken(loginUser),
                jwtUtils.getAccessTokenExpire(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar());
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        LoginUser parsed = jwtUtils.parseRefresh(refreshToken);
        if (parsed == null || parsed.userId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(parsed.userId());
        if (user == null || (user.getStatus() != null && user.getStatus() == CommonConstants.STATUS_DISABLED)) {
            throw new BizException(ResultCode.USER_DISABLED);
        }
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), LoginUser.TYPE_MEMBER,
                Set.of("MEMBER"), Set.of());
        return new LoginVO(jwtUtils.createAccessToken(loginUser),
                jwtUtils.createRefreshToken(loginUser),
                jwtUtils.getAccessTokenExpire(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar());
    }

    @Override
    public UserInfoVO getProfile(Long userId) {
        String cacheKey = CommonConstants.CACHE_USER + userId;
        UserInfoVO cached = redisCache.get(cacheKey, UserInfoVO.class);
        if (cached != null) {
            return cached;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        UserInfoVO vo = UserInfoVO.of(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(),
                user.getEmail(), user.getAvatar(), user.getMemberLevel(), user.getTotalAmount());
        redisCache.set(cacheKey, vo, Duration.ofMinutes(CommonConstants.CACHE_PRODUCT_TTL_MINUTES));
        return vo;
    }

    @Override
    public UserDTO getUser(Long userId) {
        return UserConvert.toDTO(userMapper.selectById(userId));
    }

    @Override
    public AddressDTO getAddress(Long addressId) {
        return UserConvert.toDTO(userAddressMapper.selectById(addressId));
    }

    @Override
    public List<AddressDTO> listAddress(Long userId) {
        List<UserAddress> list = userAddressMapper.selectList(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));
        return list.stream().map(UserConvert::toDTO).toList();
    }
}
