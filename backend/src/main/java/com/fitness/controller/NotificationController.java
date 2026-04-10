package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.Notification;
import com.fitness.service.NotificationService;
import com.fitness.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知管理", description = "会员通知管理接口")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "分页查询通知列表", description = "分页查询通知列表")
    @GetMapping("/page")
    public Result<com.fitness.common.PageResult<Notification>> getNotificationPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        return Result.success(notificationService.getNotificationPage(page));
    }

    @Operation(summary = "获取我的通知", description = "获取当前登录会员的通知")
    @GetMapping("/my")
    public Result<com.fitness.common.PageResult<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        return Result.success(notificationService.getMemberNotificationPage(page, memberId, unreadOnly));
    }

    @Operation(summary = "获取未读数量", description = "获取当前登录会员的未读通知数量")
    @GetMapping("/unread-count")
    public Result<java.util.Map<String, Object>> getUnreadCount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Integer count = notificationService.getUnreadCount(memberId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @Operation(summary = "标记全部已读", description = "将当前会员所有通知标记为已读")
    @PutMapping("/mark-all-read")
    public Result<Void> markAllAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationService.markAllAsRead(memberId);
        return Result.success();
    }

    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }
}
