package com.fitness.service;

import com.fitness.vo.StatisticsVO;

import java.util.Map;

/**
 * 统计Service
 */
public interface StatisticsService {

    /**
     * 获取管理员首页统计数据
     */
    StatisticsVO getAdminStatistics();

    /**
     * 获取教练业绩统计
     */
    Map<String, Object> getCoachPerformance(Long coachId, String startDate, String endDate);

    /**
     * 获取会员统计
     */
    Map<String, Object> getMemberStatistics(Long memberId);
}
