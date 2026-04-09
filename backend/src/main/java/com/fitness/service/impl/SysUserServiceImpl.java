package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.PasswordDTO;
import com.fitness.entity.Coach;
import com.fitness.entity.Member;
import com.fitness.entity.SysUser;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CoachMapper;
import com.fitness.mapper.MemberMapper;
import com.fitness.mapper.SysUserMapper;
import com.fitness.security.JwtTokenProvider;
import com.fitness.service.SysUserService;
import com.fitness.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 系统用户Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成Token
        String token = jwtTokenProvider.generateToken(authentication);
        
        // 查询用户信息
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setUserId(user.getId());
        
        // 根据角色获取详细信息
        if ("ROLE_MEMBER".equals(user.getRole())) {
            Member member = memberMapper.selectByUserId(user.getId());
            if (member != null) {
                vo.setName(member.getName());
                vo.setAvatar(member.getAvatar());
            }
        } else if ("ROLE_COACH".equals(user.getRole())) {
            Coach coach = coachMapper.selectByUserId(user.getId());
            if (coach != null) {
                vo.setName(coach.getName());
                vo.setAvatar(coach.getAvatar());
            }
        } else {
            vo.setName("管理员");
        }
        
        log.info("用户登录成功: {}", dto.getUsername());
        return vo;
    }

    @Override
    public LoginVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        LoginVO vo = new LoginVO();
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setUserId(user.getId());
        
        if ("ROLE_MEMBER".equals(user.getRole())) {
            Member member = memberMapper.selectByUserId(user.getId());
            if (member != null) {
                vo.setName(member.getName());
                vo.setAvatar(member.getAvatar());
            }
        } else if ("ROLE_COACH".equals(user.getRole())) {
            Coach coach = coachMapper.selectByUserId(user.getId());
            if (coach != null) {
                vo.setName(coach.getName());
                vo.setAvatar(coach.getAvatar());
            }
        } else {
            vo.setName("管理员");
        }
        
        return vo;
    }

    @Override
    public void updatePassword(PasswordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
        
        log.info("用户修改密码: {}", username);
    }

    @Override
    public PageResult<SysUser> pageList(Page<SysUser> page, String username, String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username);
        wrapper.eq(StringUtils.hasText(role), SysUser::getRole, role);
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> result = page(page, wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return PageResult.of(result);
    }

    @Override
    public void createUser(SysUser user) {
        // 检查用户名是否存在
        if (count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        save(user);
        
        log.info("创建用户: {}", user.getUsername());
    }

    @Override
    public void updateUser(SysUser user) {
        SysUser exist = getById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改了用户名，检查是否重复
        if (!exist.getUsername().equals(user.getUsername())) {
            if (count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, user.getUsername())) > 0) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果传了新密码则加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        
        updateById(user);
        log.info("更新用户: {}", user.getUsername());
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 默认密码为123456
        String password = StringUtils.hasText(newPassword) ? newPassword : "123456";
        user.setPassword(passwordEncoder.encode(password));
        updateById(user);
        
        log.info("管理员重置用户密码: userId={}", userId);
    }
}
