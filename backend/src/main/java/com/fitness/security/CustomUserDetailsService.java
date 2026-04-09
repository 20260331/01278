package com.fitness.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.SysUser;
import com.fitness.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        return new User(
            sysUser.getUsername(),
            sysUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(sysUser.getRole()))
        );
    }
}
