package com.ticketing.user.service.impl;

import com.ticketing.common.entity.User;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.vo.LoginVO;
import com.ticketing.common.vo.RegisterVO;
import com.ticketing.user.mapper.UserMapper;
import com.ticketing.user.service.UserService;
import com.ticketing.user.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 手机号正则表达式
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 用户名正则表达式（字母开头，允许字母数字下划线，4-16位）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z]\\w{3,15}$");
    // 密码正则表达式（包含字母和数字，8-20位）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$");
    // 短信验证码过期时间（分钟）
    private static final long SMS_CODE_EXPIRE_MINUTES = 5;
    // 短信验证码重发间隔（秒）
    private static final long SMS_CODE_RESEND_INTERVAL_SECONDS = 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterVO registerVO) {
        log.info("User registration: {}", registerVO.getUsername());
        try {
            // 参数校验
            validateRegisterParams(registerVO);

            // 检查用户名是否已存在
            if (userMapper.selectByUsername(registerVO.getUsername()) != null) {
                throw new BusinessException("用户名已存在");
            }

            // 检查手机号是否已存在
            if (userMapper.selectByMobile(registerVO.getMobile()) != null) {
                throw new BusinessException("手机号已被注册");
            }

            // 验证短信验证码
            if (!verifySmsCode(registerVO.getMobile(), registerVO.getSmsCode())) {
                throw new BusinessException("验证码无效或已过期");
            }

            // 创建用户
            User user = new User()
                    .setUsername(registerVO.getUsername())
                    .setPassword(passwordEncoder.encode(registerVO.getPassword()))
                    .setMobile(registerVO.getMobile())
                    .setNickname(registerVO.getNickname())
                    .setStatus(0)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());

            userMapper.insert(user);
            log.info("User registered successfully: {}", user.getId());
            return user.getId();
        } catch (Exception e) {
            log.error("Failed to register user: {}", registerVO.getUsername(), e);
            throw new BusinessException("注册失败: " + e.getMessage());
        }
    }

    @Override
    public String login(LoginVO loginVO) {
        log.info("User login attempt: {}", loginVO.getUsername());
        try {
            // 获取用户信息
            User user = userMapper.selectByUsername(loginVO.getUsername());
            if (user == null) {
                throw new BusinessException("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != 0) {
                throw new BusinessException("账号已被禁用");
            }

            // 生成token
            String token = jwtUtil.generateToken(user);
            log.info("User logged in successfully: {}", user.getId());
            return token;
        } catch (Exception e) {
            log.error("Failed to login: {}", loginVO.getUsername(), e);
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "#userId", unless = "#result == null")
    public User getUserInfo(Long userId) {
        log.info("Getting user info: {}", userId);
        try {
            return userMapper.selectById(userId);
        } catch (Exception e) {
            log.error("Failed to get user info: {}", userId, e);
            throw new BusinessException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#user.id")
    public boolean updateUserInfo(User user) {
        log.info("Updating user info: {}", user.getId());
        try {
            // 检查用户是否存在
            User existingUser = userMapper.selectById(user.getId());
            if (existingUser == null) {
                throw new BusinessException("用户不存在");
            }

            // 不允许修改敏感信息
            user.setUsername(existingUser.getUsername())
                    .setPassword(existingUser.getPassword())
                    .setMobile(existingUser.getMobile())
                    .setStatus(existingUser.getStatus());

            // 更新信息
            user.setUpdateTime(LocalDateTime.now());
            boolean success = userMapper.updateById(user) > 0;
            log.info("User info updated successfully: {}", user.getId());
            return success;
        } catch (Exception e) {
            log.error("Failed to update user info: {}", user.getId(), e);
            throw new BusinessException("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#userId")
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("原密码错误");
            }

            // 验证新密码格式
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                throw new BusinessException("密码必须包含字母和数字，长度8-20位");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword))
                    .setUpdateTime(LocalDateTime.now());
            boolean success = userMapper.updateById(user) > 0;
            log.info("Password changed successfully: {}", userId);
            return success;
        } catch (Exception e) {
            log.error("Failed to change password: {}", userId, e);
            throw new BusinessException("修改密码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkUsername(String username) {
        return userMapper.selectByUsername(username) == null;
    }

    @Override
    public boolean checkMobile(String mobile) {
        return userMapper.selectByMobile(mobile) == null;
    }

    @Override
    public boolean sendSmsCode(String mobile) {
        log.info("Sending SMS code to: {}", mobile);
        try {
            // 验证手机号格式
            if (!MOBILE_PATTERN.matcher(mobile).matches()) {
                throw new BusinessException("手机号格式不正确");
            }

            // 检查是否可以发送验证码
            String lastSendTimeKey = "sms:lastSendTime:" + mobile;
            String lastSendTime = redisTemplate.opsForValue().get(lastSendTimeKey);
            if (lastSendTime != null) {
                long secondsLeft = SMS_CODE_RESEND_INTERVAL_SECONDS - 
                        (System.currentTimeMillis() - Long.parseLong(lastSendTime)) / 1000;
                if (secondsLeft > 0) {
                    throw new BusinessException("请等待" + secondsLeft + "秒后重试");
                }
            }

            // 生成验证码
            String code = String.format("%06d", (int) (Math.random() * 1000000));
            String codeKey = "sms:code:" + mobile;

            // TODO: 调用短信服务发送验证码
            log.info("SMS code for {}: {}", mobile, code);

            // 保存验证码和发送时间
            redisTemplate.opsForValue().set(codeKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(lastSendTimeKey, 
                    String.valueOf(System.currentTimeMillis()), 
                    SMS_CODE_RESEND_INTERVAL_SECONDS, 
                    TimeUnit.SECONDS);

            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS code: {}", mobile, e);
            throw new BusinessException("发送验证码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySmsCode(String mobile, String code) {
        String codeKey = "sms:code:" + mobile;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(codeKey);
            return true;
        }
        return false;
    }

    /**
     * 校验注册参数
     */
    private void validateRegisterParams(RegisterVO registerVO) {
        if (!USERNAME_PATTERN.matcher(registerVO.getUsername()).matches()) {
            throw new BusinessException("用户名必须以字母开头，允许字母数字下划线，长度4-16位");
        }
        if (!PASSWORD_PATTERN.matcher(registerVO.getPassword()).matches()) {
            throw new BusinessException("密码必须包含字母和数字，长度8-20位");
        }
        if (!MOBILE_PATTERN.matcher(registerVO.getMobile()).matches()) {
            throw new BusinessException("手机号格式不正确");
        }
        if (registerVO.getNickname() == null || registerVO.getNickname().trim().isEmpty()) {
            throw new BusinessException("昵称不能为空");
        }
        if (registerVO.getSmsCode() == null || registerVO.getSmsCode().trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }
    }
} 