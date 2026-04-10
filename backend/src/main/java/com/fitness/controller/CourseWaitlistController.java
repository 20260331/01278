package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.CourseWaitlist;
import com.fitness.entity.Member;
import com.fitness.service.CourseWaitlistService;
import com.fitness.service.MemberService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程候补管理控制器
 * <p>
 * 处理课程候补的完整流程，包括：
 * - 加入候补队列
 * - 取消候补
 * - 确认补位
 * - 候补记录查询
 * </p>
 *
 * <h3>候补状态说明：</h3>
 * <ul>
 *   <li>0 - 排队中：正在等待补位</li>
 *   <li>1 - 已补位：已获得补位资格，等待确认</li>
 *   <li>2 - 已取消：候补已取消</li>
 *   <li>3 - 已过期：补位确认超时</li>
 * </ul>
 *
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "课程候补", description = "课程候补、补位确认等接口")
@RestController
@RequestMapping("/api/v1/waitlist")
@RequiredArgsConstructor
public class CourseWaitlistController {

    private final CourseWaitlistService courseWaitlistService;
    private final MemberService memberService;

    /**
     * 分页查询候补记录
     */
    @Operation(summary = "分页查询候补记录", description = "根据条件分页查询候补记录，支持按会员、课程、状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<CourseWaitlist>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        return Result.success(courseWaitlistService.pageList(new Page<>(current, size), memberId, courseId, status));
    }

    /**
     * 获取我的候补列表
     */
    @Operation(summary = "获取我的候补", description = "获取当前登录会员的候补记录列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PageResult<CourseWaitlist>> myWaitlist(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(courseWaitlistService.getMyWaitlist(new Page<>(current, size), member.getId()));
    }

    /**
     * 加入候补队列
     */
    @Operation(summary = "加入候补", description = "对已满课程加入候补队列")
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> join(@Parameter(description = "课程ID") @RequestParam Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        courseWaitlistService.joinWaitlist(member.getId(), courseId);
        return Result.success();
    }

    /**
     * 取消候补
     */
    @Operation(summary = "取消候补", description = "取消候补排队")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> cancel(@Parameter(description = "候补ID") @PathVariable Long id) {
        courseWaitlistService.cancelWaitlist(id);
        return Result.success();
    }

    /**
     * 确认补位
     */
    @Operation(summary = "确认补位", description = "候补补位成功后，确认转为正式预约")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> confirm(@Parameter(description = "候补ID") @PathVariable Long id) {
        courseWaitlistService.confirmWaitlist(id);
        return Result.success();
    }

    /**
     * 获取课程候补人数
     */
    @Operation(summary = "获取候补人数", description = "获取指定课程的候补人数")
    @GetMapping("/count/{courseId}")
    public Result<Integer> getWaitlistCount(@Parameter(description = "课程ID") @PathVariable Long courseId) {
        return Result.success(courseWaitlistService.getWaitlistCount(courseId));
    }

    /**
     * 获取课程候补队列
     */
    @Operation(summary = "获取候补队列", description = "获取指定课程的候补队列（管理员）")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CourseWaitlist>> getWaitlistByCourseId(@Parameter(description = "课程ID") @PathVariable Long courseId) {
        return Result.success(courseWaitlistService.getWaitlistByCourseId(courseId));
    }
}
