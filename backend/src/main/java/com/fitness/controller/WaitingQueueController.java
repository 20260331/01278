package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.WaitingQueue;
import com.fitness.service.WaitingQueueService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/waiting-queue")
@RequiredArgsConstructor
@Tag(name = "候补队列管理")
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @PostMapping("/join/{courseId}")
    @Operation(summary = "加入候补队列")
    public Result<Void> joinWaitingQueue(@PathVariable Long courseId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        waitingQueueService.joinWaitingQueue(memberId, courseId);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消候补")
    public Result<Void> cancelWaitingQueue(@PathVariable Long id) {
        waitingQueueService.cancelWaitingQueue(id);
        return Result.success();
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "查询课程的候补队列")
    public Result<List<WaitingQueue>> getWaitingQueueByCourseId(@PathVariable Long courseId) {
        return Result.success(waitingQueueService.getWaitingQueueByCourseId(courseId));
    }

    @GetMapping("/member-info/{courseId}")
    @Operation(summary = "查询会员在课程的候补信息")
    public Result<Map<String, Object>> getMemberWaitingInfo(@PathVariable Long courseId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return Result.success(waitingQueueService.getMemberWaitingInfo(memberId, courseId));
    }

    @GetMapping("/high-risk-courses")
    @Operation(summary = "获取未来24小时高风险课程")
    public Result<List<Map<String, Object>>> getHighRiskCourses() {
        return Result.success(waitingQueueService.getHighRiskCourses());
    }

    @GetMapping("/my-waiting")
    @Operation(summary = "查询我的候补队列")
    public Result<List<WaitingQueue>> getMyWaitingQueue() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return Result.success(waitingQueueService.getMyWaitingQueue(memberId));
    }

    @GetMapping("/unread-notified")
    @Operation(summary = "获取未读的补位成功通知")
    public Result<List<WaitingQueue>> getUnreadNotified() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return Result.success(waitingQueueService.getUnreadNotified(memberId));
    }

    @PostMapping("/mark-as-read/{id}")
    @Operation(summary = "标记补位通知为已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        waitingQueueService.markAsRead(id);
        return Result.success();
    }
}
