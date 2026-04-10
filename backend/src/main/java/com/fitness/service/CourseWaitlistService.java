package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.CourseWaitlist;

import java.util.List;

/**
 * 课程候补Service
 */
public interface CourseWaitlistService extends IService<CourseWaitlist> {

    /**
     * 分页查询候补记录
     */
    PageResult<CourseWaitlist> pageList(Page<CourseWaitlist> page, Long memberId, Long courseId, Integer status);

    /**
     * 加入候补队列
     */
    void joinWaitlist(Long memberId, Long courseId);

    /**
     * 取消候补
     */
    void cancelWaitlist(Long id);

    /**
     * 获取我的候补列表
     */
    PageResult<CourseWaitlist> getMyWaitlist(Page<CourseWaitlist> page, Long memberId);

    /**
     * 处理取消预约后的自动补位
     */
    void processWaitlistAfterCancel(Long courseId);

    /**
     * 确认补位（会员确认后转为正式预约）
     */
    void confirmWaitlist(Long id);

    /**
     * 获取课程候补人数
     */
    Integer getWaitlistCount(Long courseId);

    /**
     * 获取候补队列
     */
    List<CourseWaitlist> getWaitlistByCourseId(Long courseId);
}
