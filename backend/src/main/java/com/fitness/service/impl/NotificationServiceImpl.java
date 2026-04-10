package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.entity.Notification;
import com.fitness.mapper.NotificationMapper;
import com.fitness.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void createNotification(Long memberId, String title, String content, Integer type) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus(0);
        save(notification);

        log.info("创建通知: memberId={}, title={}", memberId, title);
    }

    @Override
    public List<Notification> getMemberNotifications(Long memberId, Boolean unreadOnly) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getMemberId, memberId)
                .orderByDesc(Notification::getCreateTime);

        if (unreadOnly) {
            wrapper.eq(Notification::getStatus, 0);
        }

        return list(wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = getById(id);
        if (notification != null) {
            notification.setStatus(1);
            updateById(notification);
        }
    }
}
