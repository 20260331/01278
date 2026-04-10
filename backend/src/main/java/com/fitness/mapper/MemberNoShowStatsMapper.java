package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.MemberNoShowStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 会员爽约统计Mapper
 */
@Mapper
public interface MemberNoShowStatsMapper extends BaseMapper<MemberNoShowStats> {

    /**
     * 根据会员ID获取统计记录
     */
    MemberNoShowStats selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 增加预约次数
     */
    int incrementTotalReservations(@Param("memberId") Long memberId);

    /**
     * 增加爽约次数并更新爽约率
     */
    int incrementNoShowCount(@Param("memberId") Long memberId);

    /**
     * 获取课程历史爽约率
     */
    BigDecimal selectCourseNoShowRate(@Param("courseId") Long courseId);
}
