package com.fitness.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.SysUser;
import com.fitness.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 安全工具类
 */
@Component
public class SecurityUtil {

    @Autowired
    private SysUserMapper sysUserMapper;
    
    private static SysUserMapper userMapper;
    
    @PostConstruct
    public void init() {
        userMapper = this.sysUserMapper;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentRole() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        return user != null ? user.getRole() : null;
    }
}
