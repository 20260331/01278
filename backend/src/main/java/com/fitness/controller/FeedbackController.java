package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Feedback;
import com.fitness.entity.Member;
import com.fitness.service.FeedbackService;
import com.fitness.service.MemberService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 意见反馈控制器
 * <p>
 * 管理会员意见反馈，包括：
 * - 提交反馈
 * - 反馈回复
 * - 反馈记录查询
 * </p>
 * 
 * <h3>反馈状态说明：</h3>
 * <ul>
 *   <li>0 - 待处理：反馈已提交，等待管理员回复</li>
 *   <li>1 - 已回复：管理员已回复反馈</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "意见反馈", description = "会员意见反馈的提交、回复、查询等接口")
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final MemberService memberService;

    /**
     * 分页查询反馈
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Feedback>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status) {
        return Result.success(feedbackService.pageList(new Page<>(current, size), memberId, status));
    }

    /**
     * 获取我的反馈
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PageResult<Feedback>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(feedbackService.pageList(new Page<>(current, size), member.getId(), null));
    }

    /**
     * 提交反馈
     */
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<Void> create(@Valid @RequestBody Feedback feedback) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        feedback.setMemberId(member.getId());
        feedbackService.createFeedback(feedback);
        return Result.success();
    }

    /**
     * 回复反馈
     */
    @Operation(summary = "回复反馈", description = "管理员回复会员的意见反馈")
    @PutMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reply(@Parameter(description = "反馈ID") @PathVariable Long id, 
                               @Parameter(description = "回复内容") @RequestParam String reply) {
        feedbackService.reply(id, reply);
        return Result.success();
    }
}
