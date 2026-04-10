package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Course;
import com.fitness.entity.CourseWaitlist;
import com.fitness.entity.Reservation;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CourseWaitlistMapper;
import com.fitness.service.CourseService;
import com.fitness.service.CourseWaitlistService;
import com.fitness.service.MemberNoShowStatsService;
import com.fitness.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程候补Service实现
 * <p>
 * 处理课程候补的核心业务逻辑，包括加入候补、取消候补和自动补位。
 * </p>
 *
 * <h3>候补状态说明：</h3>
 * <ul>
 *   <li>0 - 排队中：正在等待补位</li>
 *   <li>1 - 已补位：已获得补位资格，等待确认</li>
 *   <li>2 - 已取消：候补已取消</li>
 *   <li>3 - 已过期：补位确认超时</li>
 * </ul>
 *
 * <h3>业务规则：</h3>
 * <ol>
 *   <li>只能对已满课程加入候补</li>
 *   <li>同一会员不能重复候补同一课程</li>
 *   <li>取消预约时会自动触发补位流程</li>
 *   <li>补位成功后需在指定时间内确认</li>
 * </ol>
 *
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseWaitlistServiceImpl extends ServiceImpl<CourseWaitlistMapper, CourseWaitlist> implements CourseWaitlistService {

    private final CourseService courseService;
    @Lazy
    private final ReservationService reservationService;
    private final MemberNoShowStatsService memberNoShowStatsService;

    @Override
    public PageResult<CourseWaitlist> pageList(Page<CourseWaitlist> page, Long memberId, Long courseId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithDetail(page, memberId, courseId, status));
    }

    @Override
    public PageResult<CourseWaitlist> getMyWaitlist(Page<CourseWaitlist> page, Long memberId) {
        return PageResult.of(baseMapper.selectPageWithDetail(page, memberId, null, null));
    }

    /**
     * 加入候补队列
     * <p>
     * 业务流程：
     * 1. 检查是否重复候补
     * 2. 验证课程状态（必须已满）
     * 3. 验证课程时间（未开始）
     * 4. 获取当前排队序号
     * 5. 创建候补记录
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinWaitlist(Long memberId, Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法候补");
        }

        CourseWaitlist existing = baseMapper.selectByMemberAndCourse(memberId, courseId);
        if (existing != null) {
            throw new BusinessException("您已在该课程的候补队列中");
        }

        Long reservationCount = reservationService.count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getMemberId, memberId)
                .eq(Reservation::getCourseId, courseId)
                .ne(Reservation::getStatus, 2));
        if (reservationCount > 0) {
            throw new BusinessException("您已预约该课程");
        }

        Integer maxQueueNo = baseMapper.selectMaxQueueNo(courseId);

        CourseWaitlist waitlist = new CourseWaitlist();
        waitlist.setMemberId(memberId);
        waitlist.setCourseId(courseId);
        waitlist.setStatus(0);
        waitlist.setQueueNo(maxQueueNo + 1);
        waitlist.setNotifyStatus(0);
        save(waitlist);

        log.info("加入候补队列: memberId={}, courseId={}, queueNo={}", memberId, courseId, waitlist.getQueueNo());
    }

    /**
     * 取消候补
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitlist(Long id) {
        CourseWaitlist waitlist = getById(id);
        if (waitlist == null) {
            throw new BusinessException("候补记录不存在");
        }

        if (waitlist.getStatus() != 0 && waitlist.getStatus() != 1) {
            throw new BusinessException("该候补无法取消");
        }

        waitlist.setStatus(2);
        updateById(waitlist);

        if (waitlist.getStatus() == 0) {
            processWaitlistAfterCancel(waitlist.getCourseId());
        }

        log.info("取消候补: id={}", id);
    }

    /**
     * 处理取消预约后的自动补位
     * <p>
     * 业务流程：
     * 1. 获取该课程的候补队列（按排队顺序）
     * 2. 取第一个排队中的候补记录
     * 3. 更新候补状态为已补位
     * 4. 设置通知时间和过期时间（如2小时内确认）
     * 5. 记录通知日志
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWaitlistAfterCancel(Long courseId) {
        List<CourseWaitlist> waitlist = baseMapper.selectWaitlistByCourseId(courseId, 0);
        if (waitlist.isEmpty()) {
            return;
        }

        CourseWaitlist firstWait = waitlist.get(0);

        Course course = courseService.getById(courseId);
        if (course == null || course.getStatus() == 0 || course.getStatus() == 3) {
            log.warn("课程状态异常，无法补位: courseId={}", courseId);
            return;
        }

        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            log.warn("课程仍已满，无法补位: courseId={}", courseId);
            return;
        }

        firstWait.setStatus(1);
        firstWait.setNotifyTime(LocalDateTime.now());
        firstWait.setExpireTime(LocalDateTime.now().plusHours(2));
        firstWait.setNotifyStatus(1);
        updateById(firstWait);

        log.info("候补补位成功: waitlistId={}, memberId={}, courseId={}",
                firstWait.getId(), firstWait.getMemberId(), courseId);
    }

    /**
     * 确认补位（会员确认后转为正式预约）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWaitlist(Long id) {
        CourseWaitlist waitlist = getById(id);
        if (waitlist == null) {
            throw new BusinessException("候补记录不存在");
        }

        if (waitlist.getStatus() != 1) {
            throw new BusinessException("该候补无法确认");
        }

        if (waitlist.getExpireTime() != null && waitlist.getExpireTime().isBefore(LocalDateTime.now())) {
            waitlist.setStatus(3);
            updateById(waitlist);
            throw new BusinessException("补位确认已过期");
        }

        Course course = courseService.getById(waitlist.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            throw new BusinessException("课程名额已满，无法确认补位");
        }

        waitlist.setStatus(2);
        updateById(waitlist);

        reservationService.createReservation(waitlist.getMemberId(), waitlist.getCourseId());

        log.info("确认补位成功: waitlistId={}, memberId={}, courseId={}",
                id, waitlist.getMemberId(), waitlist.getCourseId());
    }

    @Override
    public Integer getWaitlistCount(Long courseId) {
        return baseMapper.selectWaitlistCount(courseId);
    }

    @Override
    public List<CourseWaitlist> getWaitlistByCourseId(Long courseId) {
        return baseMapper.selectWaitlistByCourseId(courseId, 0);
    }
}
