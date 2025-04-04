package com.ticketing.user.service;

import com.ticketing.common.entity.User;
import com.ticketing.common.vo.LoginVO;
import com.ticketing.common.vo.RegisterVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param registerVO 注册信息
     * @return 用户ID
     */
    Long register(RegisterVO registerVO);

    /**
     * 用户登录
     * @param loginVO 登录信息
     * @return JWT token
     */
    String login(LoginVO loginVO);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    boolean checkUsername(String username);

    /**
     * 检查手机号是否可用
     * @param mobile 手机号
     * @return 是否可用
     */
    boolean checkMobile(String mobile);

    /**
     * 发送短信验证码
     * @param mobile 手机号
     * @return 是否成功
     */
    boolean sendSmsCode(String mobile);

    /**
     * 验证短信验证码
     * @param mobile 手机号
     * @param code 验证码
     * @return 是否有效
     */
    boolean verifySmsCode(String mobile, String code);
} 