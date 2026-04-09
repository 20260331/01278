package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Coach;
import com.fitness.entity.Course;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CoachMapper;
import com.fitness.mapper.CourseMapper;
import com.fitness.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程Service实现
 * <p>
 * 管理课程的核心业务逻辑，包括课程的创建、更新、查询以及人数管理。
 * </p>
 * 
 * <h3>课程状态说明：</h3>
 * <ul>
 *   <li>0 - 已取消：课程被取消，不会显示在可预约列表中</li>
 *   <li>1 - 正常：课程正常进行，可以被预约</li>
 *   <li>2 - 已满：课程预约人数已达上限，不可预约但可取消后恢复</li>
 *   <li>3 - 已结束：课程时间已过，由定时任务更新</li>
 * </ul>
 * 
 * <h3>状态自动转换规则：</h3>
 * <ul>
 *   <li>当前人数达到最大容量时：正常(1) → 已满(2)</li>
 *   <li>当前人数低于最大容量且状态为已满时：已满(2) → 正常(1)</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CoachMapper coachMapper;

    @Override
    public PageResult<Course> pageList(Page<Course> page, String name, String type, Long coachId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithCoach(page, name, type, coachId, status));
    }

    /**
     * 获取可预约课程列表
     * <p>
     * 查询条件说明：
     * 1. status = 1：只查询状态为正常的课程
     * 2. startTime > now：只查询尚未开始的课程
     * 3. currentCount < maxCapacity：只查询未满的课程
     * </p>
     * 
     * <h4>为什么需要同时检查status和currentCount：</h4>
     * <p>
     * 虽然人数满时status会自动更新为2（已满），但为了数据安全，
     * 增加currentCount的检查可以防止并发场景下的超卖问题。
     * </p>
     * 
     * @return 可预约课程列表，按开始时间升序排列
     */
    @Override
    public List<Course> getAvailableCourses() {
        return list(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)                    // 状态为正常
                .gt(Course::getStartTime, LocalDateTime.now()) // 课程尚未开始
                .apply("current_count < max_capacity")        // 人数未满
                .orderByAsc(Course::getStartTime));           // 按开始时间排序
    }

    @Override
    public void createCourse(Course course) {
        // 验证教练是否存在
        if (course.getCoachId() != null) {
            Coach coach = coachMapper.selectById(course.getCoachId());
            if (coach == null) {
                throw new BusinessException("所选教练不存在，请重新选择");
            }
        }
        
        // 计算结束时间
        if (course.getStartTime() != null && course.getDuration() != null) {
            course.setEndTime(course.getStartTime().plusMinutes(course.getDuration()));
        }
        
        course.setCurrentCount(0);
        course.setStatus(1);
        save(course);
        log.info("创建课程: {}", course.getName());
    }

    @Override
    public void updateCourse(Course course) {
        Course exist = getById(course.getId());
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 验证教练是否存在
        if (course.getCoachId() != null) {
            Coach coach = coachMapper.selectById(course.getCoachId());
            if (coach == null) {
                throw new BusinessException("所选教练不存在，请重新选择");
            }
        }
        
        // 计算结束时间
        if (course.getStartTime() != null && course.getDuration() != null) {
            course.setEndTime(course.getStartTime().plusMinutes(course.getDuration()));
        }
        
        updateById(course);
        log.info("更新课程: {}", course.getName());
    }

    /**
     * 更新课程当前人数
     * <p>
     * 此方法在预约创建和取消时被调用，用于维护课程的当前报名人数。
     * 同时会根据人数变化自动更新课程状态。
     * </p>
     * 
     * <h4>调用场景：</h4>
     * <ul>
     *   <li>创建预约时：delta = 1（人数+1）</li>
     *   <li>取消预约时：delta = -1（人数-1）</li>
     * </ul>
     * 
     * <h4>状态自动转换逻辑：</h4>
     * <pre>
     * if (newCount >= maxCapacity) {
     *     status = 2  // 人数达到上限，标记为已满
     * } else if (status == 2) {
     *     status = 1  // 人数低于上限且之前是已满状态，恢复为正常
     * }
     * </pre>
     * 
     * <h4>为什么需要防止负数：</h4>
     * <p>
     * 在极端情况下（如并发取消、数据修复等），人数可能计算为负数，
     * 这里进行兜底处理，确保人数最小为0。
     * </p>
     * 
     * @param courseId 课程ID
     * @param delta 人数变化量（正数增加，负数减少）
     * @throws BusinessException 当课程不存在时抛出
     */
    @Override
    public void updateCurrentCount(Long courseId, int delta) {
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 计算新的人数，防止出现负数
        int newCount = course.getCurrentCount() + delta;
        if (newCount < 0) {
            newCount = 0;  // 兜底处理，人数最小为0
        }
        
        course.setCurrentCount(newCount);
        
        // ========== 自动状态转换 ==========
        // 人数达到最大容量时，自动设置为已满状态
        if (newCount >= course.getMaxCapacity()) {
            course.setStatus(2);  // 2-已满
        } 
        // 人数低于最大容量且当前是已满状态时，恢复为正常状态
        // 注意：只有已满状态(2)才会自动恢复，其他状态（如已取消、已结束）不会自动恢复
        else if (course.getStatus() == 2) {
            course.setStatus(1);  // 1-正常
        }
        
        updateById(course);
    }
}
