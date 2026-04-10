package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.entity.Course;
import com.fitness.entity.Reservation;
import com.fitness.entity.WaitingQueue;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.WaitingQueueMapper;
import com.fitness.service.CourseService;
import com.fitness.service.ReservationService;
import com.fitness.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingQueueServiceImpl extends ServiceImpl<WaitingQueueMapper, WaitingQueue> implements WaitingQueueService {

    private final CourseService courseService;
    private final ReservationService reservationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinWaitingQueue(Long memberId, Long courseId) {
        if (count(new LambdaQueryWrapper<WaitingQueue>()
                .eq(WaitingQueue::getMemberId, memberId)
                .eq(WaitingQueue::getCourseId, courseId)
                .eq(WaitingQueue::getStatus, 0)) > 0) {
            throw new BusinessException("您已在该课程的候补队列中");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        if (course.getStatus() != 1 && course.getStatus() != 2) {
            throw new BusinessException("课程不可候补");
        }

        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法候补");
        }

        Integer queueOrder = baseMapper.getNextQueueOrder(courseId);

        WaitingQueue waitingQueue = new WaitingQueue();
        waitingQueue.setMemberId(memberId);
        waitingQueue.setCourseId(courseId);
        waitingQueue.setStatus(0);
        waitingQueue.setQueueOrder(queueOrder);
        save(waitingQueue);

        log.info("加入候补队列: memberId={}, courseId={}, queueOrder={}", memberId, courseId, queueOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitingQueue(Long id) {
        WaitingQueue waitingQueue = getById(id);
        if (waitingQueue == null) {
            throw new BusinessException("候补记录不存在");
        }

        if (waitingQueue.getStatus() != 0) {
            throw new BusinessException("该候补无法取消");
        }

        waitingQueue.setStatus(2);
        updateById(waitingQueue);

        log.info("取消候补: id={}", id);
    }

    @Override
    public List<WaitingQueue> getWaitingQueueByCourseId(Long courseId) {
        return baseMapper.selectWaitingQueueWithDetail(courseId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWaitingQueueOnReservationCancel(Long courseId) {
        List<WaitingQueue> waitingList = list(new LambdaQueryWrapper<WaitingQueue>()
                .eq(WaitingQueue::getCourseId, courseId)
                .eq(WaitingQueue::getStatus, 0)
                .orderByAsc(WaitingQueue::getQueueOrder)
                .last("LIMIT 1"));

        if (!waitingList.isEmpty()) {
            WaitingQueue firstWaiting = waitingList.get(0);
            try {
                reservationService.createReservation(firstWaiting.getMemberId(), courseId);
                firstWaiting.setStatus(1);
                firstWaiting.setNotified(0);
                updateById(firstWaiting);
                log.info("候补补位成功: memberId={}, courseId={}", firstWaiting.getMemberId(), courseId);
            } catch (BusinessException e) {
                log.warn("候补补位失败，尝试下一位: memberId={}, 原因: {}", firstWaiting.getMemberId(), e.getMessage());
                firstWaiting.setStatus(3);
                updateById(firstWaiting);
                processWaitingQueueOnReservationCancel(courseId);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getHighRiskCourses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.between(Course::getStartTime, now, next24Hours);
        courseWrapper.in(Course::getStatus, 1, 2);

        List<Course> courses = courseService.list(courseWrapper);
        List<Map<String, Object>> highRiskCourses = new ArrayList<>();

        for (Course course : courses) {
            int waitingCount = count(new LambdaQueryWrapper<WaitingQueue>()
                    .eq(WaitingQueue::getCourseId, course.getId())
                    .eq(WaitingQueue::getStatus, 0));

            List<Reservation> reservations = reservationService.list(new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getCourseId, course.getId())
                    .eq(Reservation::getStatus, 0));

            double totalMissRate = 0.0;
            int validMemberCount = 0;

            for (Reservation reservation : reservations) {
                Long memberId = reservation.getMemberId();
                Integer missCount = baseMapper.getMemberMissCount(memberId);
                Integer totalCount = baseMapper.getMemberTotalReservationCount(memberId);

                if (totalCount > 0) {
                    double missRate = (double) missCount / totalCount;
                    totalMissRate += missRate;
                    validMemberCount++;
                }
            }

            double avgMissRate = validMemberCount > 0 ? totalMissRate / validMemberCount : 0.0;

            double riskScore = waitingCount * 0.4 + (avgMissRate * 100) * 0.6;

            if (riskScore > 10 || (waitingCount >= 3 && avgMissRate >= 0.2)) {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("courseId", course.getId());
                courseMap.put("courseName", course.getName());
                courseMap.put("startTime", course.getStartTime());
                courseMap.put("location", course.getLocation());
                courseMap.put("waitingCount", waitingCount);
                courseMap.put("averageMissRate", String.format("%.2f", avgMissRate * 100));
                courseMap.put("riskScore", String.format("%.2f", riskScore));
                courseMap.put("riskLevel", riskScore >= 50 ? "极高" : riskScore >= 30 ? "高" : riskScore >= 10 ? "中" : "低");
                courseMap.put("reservationCount", reservations.size());
                highRiskCourses.add(courseMap);
            }
        }

        highRiskCourses.sort((a, b) -> Double.compare(
                Double.parseDouble((String) b.get("riskScore")),
                Double.parseDouble((String) a.get("riskScore"))
        ));

        return highRiskCourses;
    }

    @Override
    public Map<String, Object> getMemberWaitingInfo(Long memberId, Long courseId) {
        Map<String, Object> info = new HashMap<>();

        int waitingCount = count(new LambdaQueryWrapper<WaitingQueue>()
                .eq(WaitingQueue::getCourseId, courseId)
                .eq(WaitingQueue::getStatus, 0));

        WaitingQueue myWaiting = getOne(new LambdaQueryWrapper<WaitingQueue>()
                .eq(WaitingQueue::getMemberId, memberId)
                .eq(WaitingQueue::getCourseId, courseId)
                .eq(WaitingQueue::getStatus, 0), false);

        info.put("waitingCount", waitingCount);
        info.put("isInQueue", myWaiting != null);
        if (myWaiting != null) {
            info.put("myQueueOrder", myWaiting.getQueueOrder());
        }

        return info;
    }

    @Override
    public List<WaitingQueue> getMyWaitingQueue(Long memberId) {
        return baseMapper.selectWaitingQueueWithMember(memberId);
    }

    @Override
    public List<WaitingQueue> getUnreadNotified(Long memberId) {
        return baseMapper.selectUnreadNotified(memberId);
    }

    @Override
    public void markAsRead(Long id) {
        WaitingQueue waitingQueue = getById(id);
        if (waitingQueue != null && waitingQueue.getNotified() != null && waitingQueue.getNotified() == 0) {
            waitingQueue.setNotified(1);
            updateById(waitingQueue);
        }
    }
}
