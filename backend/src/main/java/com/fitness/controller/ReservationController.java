package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Member;
import com.fitness.entity.Reservation;
import com.fitness.service.MemberService;
import com.fitness.service.ReservationService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 预约管理控制器
 * <p>
 * 处理课程预约的完整流程，包括：
 * - 创建预约
 * - 取消预约
 * - 课程签到
 * - 预约记录查询
 * </p>
 * 
 * <h3>预约状态说明：</h3>
 * <ul>
 *   <li>0 - 已预约：预约成功，等待上课</li>
 *   <li>1 - 已签到：会员已完成签到</li>
 *   <li>2 - 已取消：预约已被取消</li>
 *   <li>3 - 缺席：会员未签到且课程已结束</li>
 * </ul>
 * 
 * <h3>业务规则：</h3>
 * <ul>
 *   <li>同一会员不能重复预约同一课程（已取消的除外）</li>
 *   <li>课程开始后无法预约</li>
 *   <li>课程开始后无法取消预约</li>
 *   <li>签到时间窗口：课程开始前30分钟至课程结束</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "预约管理", description = "课程预约、取消、签到等接口")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    /**
     * 分页查询预约记录
     */
    @Operation(summary = "分页查询预约记录", description = "根据条件分页查询预约记录，支持按会员、课程、状态筛选")
    @GetMapping("/page")
    public Result<PageResult<Reservation>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        return Result.success(reservationService.pageList(new Page<>(current, size), memberId, courseId, status));
    }

    /**
     * 获取我的预约
     */
    @Operation(summary = "获取我的预约", description = "获取当前登录会员的预约记录列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PageResult<Reservation>> myReservations(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(reservationService.pageList(new Page<>(current, size), member.getId(), null, status));
    }

    /**
     * 创建预约
     */
    @Operation(summary = "创建预约", description = "会员预约课程，需要课程状态正常且未满")
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> create(@Parameter(description = "课程ID") @RequestParam Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        reservationService.createReservation(member.getId(), courseId);
        return Result.success();
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约", description = "取消已预约的课程，课程开始后无法取消")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> cancel(@Parameter(description = "预约ID") @PathVariable Long id) {
        reservationService.cancelReservation(id);
        return Result.success();
    }

    /**
     * 课程签到
     */
    @Operation(summary = "课程签到", description = "教练为会员进行签到，签到时间窗口为课程开始前30分钟至课程结束")
    @PutMapping("/{id}/checkin")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> checkin(@Parameter(description = "预约ID") @PathVariable Long id) {
        reservationService.checkin(id);
        return Result.success();
    }
}
