package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.entity.WaitingQueue;

import java.util.List;
import java.util.Map;

public interface WaitingQueueService extends IService<WaitingQueue> {

    void joinWaitingQueue(Long memberId, Long courseId);

    void cancelWaitingQueue(Long id);

    List<WaitingQueue> getWaitingQueueByCourseId(Long courseId);

    void processWaitingQueueOnReservationCancel(Long courseId);

    List<Map<String, Object>> getHighRiskCourses();

    Map<String, Object> getMemberWaitingInfo(Long memberId, Long courseId);

    List<WaitingQueue> getMyWaitingQueue(Long memberId);

    List<WaitingQueue> getUnreadNotified(Long memberId);

    void markAsRead(Long id);
}
