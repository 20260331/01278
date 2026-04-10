package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.entity.MemberNoShowStats;
import com.fitness.mapper.MemberNoShowStatsMapper;
import com.fitness.service.MemberNoShowStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 会员爽约统计Service实现
 *
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberNoShowStatsServiceImpl extends ServiceImpl<MemberNoShowStatsMapper, MemberNoShowStats> implements MemberNoShowStatsService {

    @Override
    public MemberNoShowStats getByMemberId(Long memberId) {
        return baseMapper.selectByMemberId(memberId);
    }

    @Override
    public void recordReservation(Long memberId) {
        baseMapper.incrementTotalReservations(memberId);
        log.debug("记录预约次数: memberId={}", memberId);
    }

    @Override
    public void recordNoShow(Long memberId) {
        baseMapper.incrementNoShowCount(memberId);
        log.info("记录爽约: memberId={}", memberId);
    }

    @Override
    public BigDecimal getCourseNoShowRate(Long courseId) {
        return baseMapper.selectCourseNoShowRate(courseId);
    }
}
