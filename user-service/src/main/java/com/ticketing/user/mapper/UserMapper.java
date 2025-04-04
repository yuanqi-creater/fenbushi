package com.ticketing.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE mobile = #{mobile} AND deleted = 0")
    User selectByMobile(String mobile);
} 