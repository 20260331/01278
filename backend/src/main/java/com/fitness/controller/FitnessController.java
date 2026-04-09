package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Coach;
import com.fitness.entity.FitnessRecord;
import com.fitness.entity.Member;
import com.fitness.service.CoachService;
import com.fitness.service.FitnessService;
import com.fitness.service.MemberService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 健身记录控制器
 * <p>
 * 管理会员的健身数据，包括：
 * - 健身数据录入
 * - 健身记录查询
 * - 健身周报生成
 * - 教练健身建议
 * </p>
 * 
 * <h3>健身记录包含的数据：</h3>
 * <ul>
 *   <li>体重记录</li>
 *   <li>运动时长</li>
 *   <li>运动类型</li>
 *   <li>消耗卡路里</li>
 *   <li>教练建议</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "健身记录", description = "健身数据录入、查询、周报、建议等接口")
@RestController
@RequestMapping("/api/v1/fitness-records")
@RequiredArgsConstructor
public class FitnessController {

    private final FitnessService fitnessService;
    private final MemberService memberService;
    private final CoachService coachService;

    /**
     * 分页查询健身记录
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('MEMBER', 'COACH')")
    public Result<PageResult<FitnessRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId) {
        return Result.success(fitnessService.pageList(new Page<>(current, size), memberId));
    }

    /**
     * 获取我的健身记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PageResult<FitnessRecord>> myRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(fitnessService.pageList(new Page<>(current, size), member.getId()));
    }

    /**
     * 录入健身数据
     */
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> create(@Valid @RequestBody FitnessRecord record) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        record.setMemberId(member.getId());
        fitnessService.createRecord(record);
        return Result.success();
    }

    /**
     * 生成健身周报
     */
    @GetMapping("/report")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Map<String, Object>> getWeeklyReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(fitnessService.getWeeklyReport(member.getId()));
    }

    /**
     * 教练发送健身建议
     */
    @Operation(summary = "发送健身建议", description = "教练为会员的健身记录添加建议")
    @PutMapping("/{id}/advice")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> sendAdvice(@Parameter(description = "健身记录ID") @PathVariable Long id, 
                                    @Parameter(description = "健身建议") @RequestParam String advice) {
        Long userId = SecurityUtil.getCurrentUserId();
        Coach coach = coachService.getByUserId(userId);
        fitnessService.sendAdvice(id, advice, coach.getId());
        return Result.success();
    }
}
