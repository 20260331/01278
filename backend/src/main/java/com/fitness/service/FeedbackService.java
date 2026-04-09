package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Feedback;

/**
 * 反馈Service
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 分页查询反馈
     */
    PageResult<Feedback> pageList(Page<Feedback> page, Long memberId, Integer status);

    /**
     * 提交反馈
     */
    void createFeedback(Feedback feedback);

    /**
     * 回复反馈
     */
    void reply(Long id, String reply);
}
