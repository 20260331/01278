package com.fitness.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计数据VO
 */
@Data
public class StatisticsVO {
    private Long memberCount;
    private Long coachCount;
    private Long courseCount;
    private Long todayCourseCount;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private Long pendingFeedback;
    private Long pendingLeave;
}
