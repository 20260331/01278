package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Member;
import com.fitness.entity.SysUser;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.MemberMapper;
import com.fitness.mapper.SysUserMapper;
import com.fitness.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<Member> pageList(Page<Member> page, String name, String phone, Integer status) {
        return PageResult.of(baseMapper.selectPageWithUsername(page, name, phone, status));
    }

    @Override
    public Member getByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMember(Member member, String username, String password) {
        // 检查用户名是否存在
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户账号
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_MEMBER");
        user.setStatus(1);
        sysUserMapper.insert(user);
        
        // 创建会员
        member.setUserId(user.getId());
        member.setStatus(1);
        save(member);
        
        log.info("创建会员: {}, 用户名: {}", member.getName(), username);
    }

    @Override
    public void updateMember(Member member) {
        Member exist = getById(member.getId());
        if (exist == null) {
            throw new BusinessException("会员不存在");
        }
        updateById(member);
        log.info("更新会员: {}", member.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Member member = getById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        
        member.setStatus(status);
        updateById(member);
        
        // 同时更新用户状态
        SysUser user = sysUserMapper.selectById(member.getUserId());
        if (user != null) {
            user.setStatus(status);
            sysUserMapper.updateById(user);
        }
        
        log.info("更新会员状态: {}, status={}", member.getName(), status);
    }
}
