package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.PasswordDTO;
import com.fitness.service.SysUserService;
import com.fitness.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * <p>
 * 处理用户认证相关操作，包括登录、登出、获取用户信息、修改密码等。
 * 登录成功后返回JWT令牌，用于后续接口的身份验证。
 * </p>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户登录、登出、密码修改等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统，返回JWT令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(sysUserService.login(dto));
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<LoginVO> getCurrentUser() {
        return Result.success(sysUserService.getCurrentUser());
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        sysUserService.updatePassword(dto);
        return Result.success();
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
