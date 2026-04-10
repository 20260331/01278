package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.WaitingList;
import com.fitness.service.WaitingListService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "候补管理", description = "课程候补队列管理接口")
@RestController
@RequestMapping("/api/v1/waiting-list")
@RequiredArgsConstructor
public class WaitingListController {

    private final WaitingListService waitingListService;

    @Operation(summary = "分页查询候补记录", description = "分页查询课程候补队列记录")
    @GetMapping("/page")
    public Result<PageResult<WaitingList>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        Page<WaitingList> page = new Page<>(pageNum, pageSize);
        return Result.success(waitingListService.pageList(page, memberId, courseId, status));
    }

    @Operation(summary = "查询我的候补记录", description = "获取当前登录会员的候补记录")
    @GetMapping("/my")
    public Result<PageResult<WaitingList>> getMyWaitingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Page<WaitingList> page = new Page<>(pageNum, pageSize);
        return Result.success(waitingListService.pageList(page, memberId, null, status));
    }

    @Operation(summary = "加入候补队列", description = "会员加入指定课程的候补队列")
    @PostMapping
    public Result<Void> joinWaitingList(@RequestParam Long courseId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        waitingListService.joinWaitingList(memberId, courseId);
        return Result.success();
    }

    @Operation(summary = "取消候补", description = "从课程候补队列中取消")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelWaitingList(@PathVariable Long id) {
        waitingListService.cancelWaitingList(id);
        return Result.success();
    }
}
