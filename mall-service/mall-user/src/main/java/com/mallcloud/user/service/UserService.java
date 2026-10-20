package com.mallcloud.user.service;

import com.mallcloud.api.user.dto.AddressDTO;
import com.mallcloud.api.user.dto.UserDTO;
import com.mallcloud.user.dto.LoginDTO;
import com.mallcloud.user.dto.RegisterDTO;
import com.mallcloud.user.vo.LoginVO;
import com.mallcloud.user.vo.UserInfoVO;

import java.util.List;

/**
 * 会员服务。接口与实现分离，便于后续替换或加缓存代理。
 */
public interface UserService {

    Long register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    LoginVO refresh(String refreshToken);

    UserInfoVO getProfile(Long userId);

    UserDTO getUser(Long userId);

    AddressDTO getAddress(Long addressId);

    List<AddressDTO> listAddress(Long userId);
}
