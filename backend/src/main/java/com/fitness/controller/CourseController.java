package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Coach;
import com.fitness.entity.CoachLeave;
import com.fitness.entity.Course;
import com.fitness.service.CoachLeaveService;
import com.fitness.service.CoachService;
import com.fitness.service.CourseService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 课程管理控制器
 * <p>
 * 提供课程的完整管理功能，包括：
 * - 课程的增删改查
 * - 可预约课程查询
 * - 教练请假/调课申请管理
 * </p>
 * 
 * <h3>课程状态说明：</h3>
 * <ul>
 *   <li>0 - 已取消：课程已取消，不可预约</li>
 *   <li>1 - 正常：课程正常进行中，可以预约</li>
 *   <li>2 - 已满：课程人数已满，不可预约</li>
 *   <li>3 - 已结束：课程已结束</li>
 * </ul>
 * 
 * <h3>请假申请状态说明：</h3>
 * <ul>
 *   <li>0 - 待审核：申请已提交，等待管理员审核</li>
 *   <li>1 - 已通过：申请已批准</li>
 *   <li>2 - 已拒绝：申请被拒绝</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "课程管理", description = "课程的增删改查、请假调课管理等接口")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CoachService coachService;
    private final CoachLeaveService coachLeaveService;

    /**
     * 分页查询课程
     */
    @Operation(summary = "分页查询课程", description = "根据条件分页查询课程列表")
    @GetMapping("/page")
    public Result<PageResult<Course>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) Integer status) {
        return Result.success(courseService.pageList(new Page<>(current, size), name, type, coachId, status));
    }

    /**
     * 获取可预约课程
     */
    @Operation(summary = "获取可预约课程", description = "获取当前可预约的课程列表")
    @GetMapping("/available")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<Course>> getAvailableCourses() {
        return Result.success(courseService.getAvailableCourses());
    }

    /**
     * 获取课程详情
     */
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    public Result<Course> getById(@Parameter(description = "课程ID") @PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    /**
     * 新增课程
     */
    @Operation(summary = "新增课程", description = "创建新的课程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody Course course) {
        courseService.createCourse(course);
        return Result.success();
    }

    /**
     * 修改课程
     */
    @Operation(summary = "修改课程", description = "更新课程信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody Course course) {
        courseService.updateCourse(course);
        return Result.success();
    }

    /**
     * 删除课程
     */
    @Operation(summary = "删除课程", description = "删除指定课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "课程ID") @PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    /**
     * 教练提交请假/调课申请
     */
    @Operation(summary = "提交请假/调课申请", description = "教练提交请假或调课申请")
    @PostMapping("/leave")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> submitLeave(@Valid @RequestBody CoachLeave leave) {
        Long userId = SecurityUtil.getCurrentUserId();
        Coach coach = coachService.getByUserId(userId);
        leave.setCoachId(coach.getId());
        coachLeaveService.createLeave(leave);
        return Result.success();
    }

    /**
     * 审核请假/调课申请
     */
    @Operation(summary = "审核请假/调课申请", description = "管理员审核教练的请假或调课申请")
    @PutMapping("/leave/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditLeave(
            @Parameter(description = "申请ID") @PathVariable Long id, 
            @Parameter(description = "审核状态：1-通过 2-拒绝") @RequestParam Integer status,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark) {
        coachLeaveService.audit(id, status, remark);
        return Result.success();
    }

    /**
     * 获取请假申请列表
     */
    @Operation(summary = "获取请假申请列表", description = "分页查询请假/调课申请列表")
    @GetMapping("/leave/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public Result<PageResult<CoachLeave>> leaveList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) Integer status) {
        return Result.success(coachLeaveService.pageList(new Page<>(current, size), coachId, status));
    }

    /**
     * 检查教练在指定时间段是否请假
     */
    @Operation(summary = "检查教练请假状态", description = "检查教练在指定时间段是否有请假记录")
    @GetMapping("/leave/check")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CoachLeave> checkCoachLeave(
            @RequestParam Long coachId,
            @RequestParam String startTime,
            @RequestParam Integer duration) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTime, 
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        java.time.LocalDateTime end = start.plusMinutes(duration);
        CoachLeave leave = coachLeaveService.checkLeave(coachId, start, end);
        return Result.success(leave);
    }
}
