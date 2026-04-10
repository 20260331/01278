package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.entity.MemberNoShowStats;

import java.math.BigDecimal;

/**
 * 会员爽约统计Service
 */
public interface MemberNoShowStatsService extends IService<MemberNoShowStats> {

    /**
     * 获取会员爽约统计
     */
    MemberNoShowStats getByMemberId(Long memberId);

    /**
     * 记录一次预约
     */
    void recordReservation(Long memberId);

    /**
     * 记录一次爽约
     */
    void recordNoShow(Long memberId);

    /**
     * 获取课程历史爽约率
     */
    BigDecimal getCourseNoShowRate(Long courseId);
}
