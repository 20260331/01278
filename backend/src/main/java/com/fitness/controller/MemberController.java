package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Member;
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
 * 会员管理控制器
 * <p>
 * 提供会员的完整生命周期管理，包括：
 * - 会员信息的增删改查
 * - 会员状态管理（冻结/激活）
 * - 会员等级管理
 * </p>
 * 
 * <h3>会员状态说明：</h3>
 * <ul>
 *   <li>0 - 冻结：会员账号被冻结，无法登录和使用系统</li>
 *   <li>1 - 正常：会员账号正常，可以正常使用系统功能</li>
 * </ul>
 * 
 * <h3>会员等级说明：</h3>
 * <ul>
 *   <li>1 - 普通会员</li>
 *   <li>2 - 银卡会员</li>
 *   <li>3 - 金卡会员</li>
 *   <li>4 - 钻石会员</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "会员管理", description = "会员信息的增删改查、状态管理等接口")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 分页查询会员
     */
    @Operation(summary = "分页查询会员", description = "根据条件分页查询会员列表，支持按姓名、手机号、状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public Result<PageResult<Member>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "会员姓名") @RequestParam(required = false) String name,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态：0-冻结 1-正常") @RequestParam(required = false) Integer status) {
        return Result.success(memberService.pageList(new Page<>(current, size), name, phone, status));
    }

    /**
     * 获取会员详情
     */
    @Operation(summary = "获取会员详情", description = "根据会员ID获取会员详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public Result<Member> getById(@Parameter(description = "会员ID") @PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    /**
     * 获取当前会员信息
     */
    @Operation(summary = "获取当前会员信息", description = "获取当前登录会员的详细信息")
    @GetMapping("/current")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Member> getCurrentMember() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(memberService.getByUserId(userId));
    }

    /**
     * 新增会员
     */
    @Operation(summary = "新增会员", description = "创建新会员账号，同时创建系统用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody Map<String, Object> params) {
        Member member = new Member();
        member.setName((String) params.get("name"));
        member.setGender((Integer) params.get("gender"));
        member.setPhone((String) params.get("phone"));
        member.setLevel((Integer) params.get("level"));
        
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        
        memberService.createMember(member, username, password);
        return Result.success();
    }

    /**
     * 修改会员信息
     */
    @Operation(summary = "修改会员信息", description = "更新会员的基本信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public Result<Void> update(@Valid @RequestBody Member member) {
        memberService.updateMember(member);
        return Result.success();
    }

    /**
     * 修改会员状态
     */
    @Operation(summary = "修改会员状态", description = "冻结或激活会员账号")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(
            @Parameter(description = "会员ID") @PathVariable Long id, 
            @Parameter(description = "状态：0-冻结 1-正常") @RequestParam Integer status) {
        memberService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除会员
     */
    @Operation(summary = "删除会员", description = "删除会员及其关联的用户账号")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "会员ID") @PathVariable Long id) {
        memberService.removeById(id);
        return Result.success();
    }
}
