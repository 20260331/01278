package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Coach;
import com.fitness.entity.Member;
import com.fitness.entity.SysLog;
import com.fitness.mapper.SysLogMapper;
import com.fitness.service.CoachService;
import com.fitness.service.MemberService;
import com.fitness.service.StatisticsService;
import com.fitness.util.SecurityUtil;
import com.fitness.vo.HighRiskCourseVO;
import com.fitness.vo.StatisticsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 * <p>
 * 提供系统各类统计数据，包括：
 * - 管理员首页统计
 * - 教练业绩统计
 * - 会员数据统计
 * - 系统日志查询
 * </p>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "统计分析", description = "系统各类统计数据、日志查询等接口")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final CoachService coachService;
    private final MemberService memberService;
    private final SysLogMapper sysLogMapper;

    /**
     * 获取管理员首页统计
     */
    @Operation(summary = "获取管理员首页统计", description = "获取管理员首页展示的各类统计数据")
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsVO> getAdminStatistics() {
        return Result.success(statisticsService.getAdminStatistics());
    }

    /**
     * 获取教练业绩统计
     */
    @Operation(summary = "获取教练业绩统计", description = "获取当前教练在指定时间范围内的业绩统计")
    @GetMapping("/coach/performance")
    @PreAuthorize("hasRole('COACH')")
    public Result<Map<String, Object>> getCoachPerformance(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        Coach coach = coachService.getByUserId(userId);
        if (coach == null) {
            throw new com.fitness.exception.BusinessException("教练信息不存在，请联系管理员");
        }
        return Result.success(statisticsService.getCoachPerformance(coach.getId(), startDate, endDate));
    }

    /**
     * 获取会员统计
     */
    @Operation(summary = "获取会员统计", description = "获取当前会员的个人统计数据")
    @GetMapping("/member/dashboard")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Map<String, Object>> getMemberStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            throw new com.fitness.exception.BusinessException("会员信息不存在，请联系管理员");
        }
        return Result.success(statisticsService.getMemberStatistics(member.getId()));
    }

    /**
     * 获取系统日志
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        Page<SysLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLog::getUsername, username);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return Result.success(PageResult.of(sysLogMapper.selectPage(page, wrapper)));
    }

    /**
     * 获取未来24小时高风险课程
     */
    @Operation(summary = "获取高风险课程", description = "获取未来24小时内的高风险课程列表（结合候补人数与历史爽约率）")
    @GetMapping("/high-risk-courses")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<HighRiskCourseVO>> getHighRiskCourses() {
        return Result.success(statisticsService.getHighRiskCourses());
    }
}
