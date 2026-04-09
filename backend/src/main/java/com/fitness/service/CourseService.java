package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Course;

import java.util.List;

/**
 * 课程Service
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程
     */
    PageResult<Course> pageList(Page<Course> page, String name, String type, Long coachId, Integer status);

    /**
     * 获取可预约课程列表
     */
    List<Course> getAvailableCourses();

    /**
     * 创建课程
     */
    void createCourse(Course course);

    /**
     * 更新课程
     */
    void updateCourse(Course course);

    /**
     * 更新课程预约人数
     */
    void updateCurrentCount(Long courseId, int delta);
}
