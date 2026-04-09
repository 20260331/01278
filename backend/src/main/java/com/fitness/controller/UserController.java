package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.SysUser;
import com.fitness.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 * <p>
 * 管理系统用户账号，包括：
 * - 用户的增删改查
 * - 密码重置
 * </p>
 * 
 * <h3>用户角色说明：</h3>
 * <ul>
 *   <li>ADMIN - 管理员：拥有系统全部权限</li>
 *   <li>COACH - 教练：管理课程、查看学员等</li>
 *   <li>MEMBER - 会员：预约课程、健身记录等</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "系统用户账号的增删改查、密码重置等接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        return Result.success(sysUserService.pageList(new Page<>(current, size), username, role));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SysUser user) {
        sysUserService.createUser(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "重置指定用户的密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id, 
                                       @Parameter(description = "新密码，不填则使用默认密码") @RequestParam(required = false) String newPassword) {
        sysUserService.resetPassword(id, newPassword);
        return Result.success();
    }
}
