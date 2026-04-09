package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Coach;
import com.fitness.entity.Course;
import com.fitness.entity.SysUser;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CoachMapper;
import com.fitness.mapper.CourseMapper;
import com.fitness.mapper.SysUserMapper;
import com.fitness.service.CoachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教练Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    private final SysUserMapper sysUserMapper;
    private final CourseMapper courseMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<Coach> pageList(Page<Coach> page, String name, Integer status) {
        return PageResult.of(baseMapper.selectPageWithUsername(page, name, status));
    }

    @Override
    public Coach getByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoach(Coach coach, String username, String password) {
        // 检查用户名是否存在
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户账号
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_COACH");
        user.setStatus(1);
        sysUserMapper.insert(user);
        
        // 创建教练（默认待审核）
        coach.setUserId(user.getId());
        coach.setStatus(0);
        save(coach);
        
        log.info("创建教练: {}, 用户名: {}", coach.getName(), username);
    }

    @Override
    public void updateCoach(Coach coach) {
        Coach exist = getById(coach.getId());
        if (exist == null) {
            throw new BusinessException("教练不存在");
        }
        updateById(coach);
        log.info("更新教练: {}", coach.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status) {
        Coach coach = getById(id);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }
        
        coach.setStatus(status);
        updateById(coach);
        
        // 更新用户状态
        SysUser user = sysUserMapper.selectById(coach.getUserId());
        if (user != null) {
            user.setStatus(status == 1 ? 1 : 0);
            sysUserMapper.updateById(user);
        }
        
        log.info("审核教练: {}, status={}", coach.getName(), status);
    }

    @Override
    public List<Course> getSchedule(Long coachId) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getCoachId, coachId)
                .ge(Course::getStartTime, LocalDateTime.now())
                .orderByAsc(Course::getStartTime));
    }
}
