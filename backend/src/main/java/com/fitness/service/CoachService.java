package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Coach;
import com.fitness.entity.Course;

import java.util.List;

/**
 * 教练Service
 */
public interface CoachService extends IService<Coach> {

    /**
     * 分页查询教练
     */
    PageResult<Coach> pageList(Page<Coach> page, String name, Integer status);

    /**
     * 根据用户ID获取教练
     */
    Coach getByUserId(Long userId);

    /**
     * 创建教练（同时创建用户账号）
     */
    void createCoach(Coach coach, String username, String password);

    /**
     * 更新教练信息
     */
    void updateCoach(Coach coach);

    /**
     * 审核教练资质
     */
    void audit(Long id, Integer status);

    /**
     * 获取教练排班（课程列表）
     */
    List<Course> getSchedule(Long coachId);
}
