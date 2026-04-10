package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    void createNotification(Long memberId, String title, String content, Integer type);

    List<Notification> getMemberNotifications(Long memberId, Boolean unreadOnly);

    void markAsRead(Long id);
}
