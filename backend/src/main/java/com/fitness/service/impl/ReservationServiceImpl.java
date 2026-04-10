package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Course;
import com.fitness.entity.Reservation;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.ReservationMapper;
import com.fitness.service.CourseService;
import com.fitness.service.ReservationService;
import com.fitness.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预约Service实现
 * <p>
 * 处理课程预约的核心业务逻辑，包括创建预约、取消预约和签到。
 * </p>
 * 
 * <h3>预约状态说明：</h3>
 * <ul>
 *   <li>0 - 已预约：预约成功，等待上课签到</li>
 *   <li>1 - 已签到：会员已完成课程签到</li>
 *   <li>2 - 已取消：预约已被会员或系统取消</li>
 *   <li>3 - 缺席：会员未签到且课程已结束（由定时任务更新）</li>
 * </ul>
 * 
 * <h3>业务规则：</h3>
 * <ol>
 *   <li>同一会员不能重复预约同一课程（已取消的预约除外）</li>
 *   <li>只能预约状态正常(status=1)且未满的课程</li>
 *   <li>课程开始后无法预约和取消</li>
 *   <li>签到时间窗口：课程开始前30分钟至课程结束</li>
 *   <li>预约/取消时会自动更新课程的当前人数</li>
 * </ol>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final CourseService courseService;
    private final WaitingQueueService waitingQueueService;

    @Override
    public PageResult<Reservation> pageList(Page<Reservation> page, Long memberId, Long courseId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithDetail(page, memberId, courseId, status));
    }

    /**
     * 创建课程预约
     * <p>
     * 业务流程：
     * 1. 检查是否重复预约（排除已取消的预约，status != 2）
     * 2. 验证课程状态（必须为正常状态 status=1）
     * 3. 验证课程容量（当前人数 < 最大容量）
     * 4. 验证课程时间（课程未开始）
     * 5. 创建预约记录（初始状态为0-已预约）
     * 6. 更新课程当前人数（+1）
     * </p>
     * 
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @throws BusinessException 当预约条件不满足时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReservation(Long memberId, Long courseId) {
        // ========== 1. 检查是否重复预约 ==========
        // 查询条件：同一会员、同一课程、且状态不是已取消(2)
        // 已取消的预约允许重新预约
        if (count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getMemberId, memberId)
                .eq(Reservation::getCourseId, courseId)
                .ne(Reservation::getStatus, 2)) > 0) {
            throw new BusinessException("您已预约该课程");
        }
        
        // ========== 2. 验证课程信息 ==========
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        // 课程状态：0-已取消, 1-正常, 2-已满, 3-已结束
        // 只有状态为1（正常）的课程才能预约
        if (course.getStatus() != 1) {
            throw new BusinessException("课程不可预约");
        }
        // 验证课程容量
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            throw new BusinessException("课程已满");
        }
        // 验证课程时间
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法预约");
        }
        
        // ========== 3. 创建预约记录 ==========
        Reservation reservation = new Reservation();
        reservation.setMemberId(memberId);
        reservation.setCourseId(courseId);
        reservation.setStatus(0);  // 0-已预约
        save(reservation);
        
        // ========== 4. 更新课程人数 ==========
        // delta=1 表示增加1人，课程人数满后会自动更新课程状态为2（已满）
        courseService.updateCurrentCount(courseId, 1);
        
        log.info("创建预约: memberId={}, courseId={}", memberId, courseId);
    }

    /**
     * 取消课程预约
     * <p>
     * 业务流程：
     * 1. 验证预约记录存在
     * 2. 验证预约状态（只有状态为0-已预约的才能取消）
     * 3. 验证课程时间（课程开始后不能取消）
     * 4. 更新预约状态为2-已取消，记录取消时间
     * 5. 更新课程当前人数（-1）
     * </p>
     * 
     * <h4>取消后的影响：</h4>
     * <ul>
     *   <li>预约状态变为2（已取消）</li>
     *   <li>课程当前人数减少1</li>
     *   <li>如果课程之前是已满状态(2)，会自动恢复为正常状态(1)</li>
     *   <li>会员可以重新预约该课程</li>
     * </ul>
     * 
     * @param id 预约记录ID
     * @throws BusinessException 当取消条件不满足时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        
        // 只有状态为0（已预约）的预约才能取消
        // 状态1（已签到）、状态2（已取消）、状态3（缺席）都不能取消
        if (reservation.getStatus() != 0) {
            throw new BusinessException("该预约无法取消");
        }
        
        // 检查课程是否已开始，已开始的课程不允许取消预约
        Course course = courseService.getById(reservation.getCourseId());
        if (course != null && course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法取消");
        }
        
        // 更新预约状态
        reservation.setStatus(2);  // 2-已取消
        reservation.setCancelTime(LocalDateTime.now());
        updateById(reservation);
        
        // 更新课程人数，delta=-1 表示减少1人
        // 这会触发课程状态检查，如果课程之前是已满状态会自动恢复
        courseService.updateCurrentCount(reservation.getCourseId(), -1);

        // 触发候补队列处理
        waitingQueueService.processWaitingQueueOnReservationCancel(reservation.getCourseId());
        
        log.info("取消预约: id={}", id);
    }

    /**
     * 课程签到
     * <p>
     * 业务流程：
     * 1. 验证预约记录存在
     * 2. 验证预约状态（只有状态为0-已预约的才能签到）
     * 3. 验证签到时间窗口
     * 4. 更新预约状态为1-已签到，记录签到时间
     * </p>
     * 
     * <h4>签到时间窗口：</h4>
     * <ul>
     *   <li>最早签到时间：课程开始前30分钟</li>
     *   <li>最晚签到时间：课程结束时间（开始时间 + 课程时长）</li>
     * </ul>
     * 
     * <h4>设计说明：</h4>
     * <p>
     * 设置30分钟提前量是为了：
     * 1. 允许会员提前到场准备
     * 2. 方便教练在课程开始前确认出勤情况
     * 3. 避免课程开始后才发现会员未到场
     * </p>
     * 
     * @param id 预约记录ID
     * @throws BusinessException 当签到条件不满足时抛出
     */
    @Override
    public void checkin(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        
        // 只有状态为0（已预约）的预约才能签到
        if (reservation.getStatus() != 0) {
            throw new BusinessException("该预约状态无法签到");
        }
        
        // 获取课程信息以计算签到时间窗口
        Course course = courseService.getById(reservation.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // ========== 计算签到时间窗口 ==========
        LocalDateTime now = LocalDateTime.now();
        // 最早签到时间 = 课程开始时间 - 30分钟
        LocalDateTime earliestCheckin = course.getStartTime().minusMinutes(30);
        // 最晚签到时间 = 课程开始时间 + 课程时长（分钟）
        LocalDateTime latestCheckin = course.getStartTime().plusMinutes(course.getDuration());
        
        // 验证签到时间
        if (now.isBefore(earliestCheckin)) {
            throw new BusinessException("签到未开始，请在课程开始前30分钟内签到");
        }
        if (now.isAfter(latestCheckin)) {
            throw new BusinessException("签到已结束，课程已结束");
        }
        
        // 更新预约状态为已签到
        reservation.setStatus(1);  // 1-已签到
        reservation.setCheckinTime(LocalDateTime.now());
        updateById(reservation);
        
        log.info("课程签到: id={}", id);
    }
}
