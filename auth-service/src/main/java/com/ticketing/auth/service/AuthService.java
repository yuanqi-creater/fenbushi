package com.ticketing.auth.service;

import com.ticketing.auth.model.LoginRequest;
import com.ticketing.auth.model.RegisterRequest;
import com.ticketing.auth.model.TokenResponse;
import com.ticketing.auth.model.UserInfo;

public interface AuthService {
    /**
     * 用户注册
     */
    UserInfo register(RegisterRequest request);

    /**
     * 用户登录
     */
    TokenResponse login(LoginRequest request);

    /**
     * 刷新token
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    void logout(String token);

    /**
     * 验证token
     */
    UserInfo validateToken(String token);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码（通过手机验证码）
     */
    void resetPassword(String phone, String verificationCode, String newPassword);
} 