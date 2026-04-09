package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Feedback;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.FeedbackMapper;
import com.fitness.service.FeedbackService;
import com.fitness.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 反馈Service实现
 */
@Slf4j
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    public PageResult<Feedback> pageList(Page<Feedback> page, Long memberId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithMember(page, memberId, status));
    }

    @Override
    public void createFeedback(Feedback feedback) {
        feedback.setStatus(0);
        save(feedback);
        log.info("提交反馈: memberId={}", feedback.getMemberId());
    }

    @Override
    public void reply(Long id, String reply) {
        Feedback feedback = getById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        
        feedback.setReply(reply);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setReplyUserId(SecurityUtil.getCurrentUserId());
        feedback.setStatus(1);
        updateById(feedback);
        
        log.info("回复反馈: id={}", id);
    }
}
