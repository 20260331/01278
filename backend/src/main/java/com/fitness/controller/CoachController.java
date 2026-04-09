package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Coach;
import com.fitness.entity.Course;
import com.fitness.service.CoachService;
import com.fitness.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 教练管理控制器
 * <p>
 * 提供教练的完整管理功能，包括：
 * - 教练信息的增删改查
 * - 教练审核
 * - 教练排班查询
 * </p>
 * 
 * <h3>教练状态说明：</h3>
 * <ul>
 *   <li>0 - 待审核：新增教练等待管理员审核</li>
 *   <li>1 - 正常：教练已通过审核，可以正常工作</li>
 *   <li>2 - 已离职：教练已离职</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "教练管理", description = "教练信息的增删改查、审核、排班等接口")
@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    /**
     * 分页查询教练
     */
    @Operation(summary = "分页查询教练", description = "根据条件分页查询教练列表")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Coach>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.success(coachService.pageList(new Page<>(current, size), name, status));
    }

    /**
     * 获取所有教练（下拉选择用）
     */
    @GetMapping("/list")
    public Result<List<Coach>> list() {
        return Result.success(coachService.list());
    }

    /**
     * 获取教练详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Coach> getById(@PathVariable Long id) {
        return Result.success(coachService.getById(id));
    }

    /**
     * 获取当前教练信息
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('COACH')")
    public Result<Coach> getCurrentCoach() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(coachService.getByUserId(userId));
    }

    /**
     * 新增教练
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody Map<String, Object> params) {
        Coach coach = new Coach();
        coach.setName((String) params.get("name"));
        coach.setGender((Integer) params.get("gender"));
        coach.setPhone((String) params.get("phone"));
        coach.setSpecialty((String) params.get("specialty"));
        
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        
        coachService.createCoach(coach, username, password);
        return Result.success();
    }

    /**
     * 修改教练信息
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public Result<Void> update(@Valid @RequestBody Coach coach) {
        coachService.updateCoach(coach);
        return Result.success();
    }

    /**
     * 审核教练
     */
    @Operation(summary = "审核教练", description = "审核教练申请，通过或拒绝")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> audit(@Parameter(description = "教练ID") @PathVariable Long id, 
                              @Parameter(description = "审核状态：1-通过 2-拒绝") @RequestParam Integer status) {
        coachService.audit(id, status);
        return Result.success();
    }

    /**
     * 获取教练排班
     */
    @Operation(summary = "获取教练排班", description = "获取指定教练的课程排班列表")
    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public Result<List<Course>> getSchedule(@Parameter(description = "教练ID") @PathVariable Long id) {
        return Result.success(coachService.getSchedule(id));
    }

    /**
     * 删除教练
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        coachService.removeById(id);
        return Result.success();
    }
}
