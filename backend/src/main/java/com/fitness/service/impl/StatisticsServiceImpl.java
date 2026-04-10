package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.service.StatisticsService;
import com.fitness.vo.HighRiskCourseVO;
import com.fitness.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计Service实现
 * <p>
 * 提供系统各类统计数据的计算和查询服务。
 * </p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>管理员首页统计：会员数、教练数、课程数、收支等</li>
 *   <li>教练业绩统计：课程数、学员数、收入、提成等</li>
 *   <li>会员个人统计：预约数、完成数、到期提醒等</li>
 * </ul>
 * 
 * <h3>薪资计算规则：</h3>
 * <ul>
 *   <li>基本薪资：教练表中设定的月基本工资</li>
 *   <li>提成比例：固定为10%（0.10）</li>
 *   <li>课程收入 = Σ(课程价格 × 实际报名人数)</li>
 *   <li>提成 = 课程收入 × 提成比例</li>
 *   <li>预计总收入 = 基本薪资 + 提成</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;
    private final FeedbackMapper feedbackMapper;
    private final CoachLeaveMapper coachLeaveMapper;
    private final ConsumeRecordMapper consumeRecordMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public StatisticsVO getAdminStatistics() {
        StatisticsVO vo = new StatisticsVO();
        
        // 会员数量
        vo.setMemberCount(memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getStatus, 1)));
        
        // 教练数量
        vo.setCoachCount(coachMapper.selectCount(
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1)));
        
        // 课程数量
        vo.setCourseCount(courseMapper.selectCount(null));
        
        // 今日课程
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        vo.setTodayCourseCount(courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .ge(Course::getStartTime, todayStart)
                        .lt(Course::getStartTime, todayEnd)));
        
        // 本月收支
        String monthStart = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
        String monthEnd = LocalDate.now().plusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
        vo.setMonthIncome(consumeRecordMapper.sumIncome(monthStart, monthEnd));
        vo.setMonthExpense(consumeRecordMapper.sumExpense(monthStart, monthEnd));
        
        // 待处理反馈
        vo.setPendingFeedback(feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>().eq(Feedback::getStatus, 0)));
        
        // 待审核请假
        vo.setPendingLeave(coachLeaveMapper.selectCount(
                new LambdaQueryWrapper<CoachLeave>().eq(CoachLeave::getStatus, 0)));
        
        return vo;
    }

    /**
     * 获取教练业绩统计
     * <p>
     * 统计指定教练在指定时间范围内的业绩数据。
     * </p>
     * 
     * <h4>计算公式：</h4>
     * <pre>
     * 1. 课程收入 = Σ(每节课价格 × 实际报名人数)
     *    - 这里使用currentCount而非maxCapacity，因为要统计实际收入
     * 
     * 2. 提成 = 课程收入 × 提成比例(10%)
     *    - 提成比例目前为固定值，后续可考虑从配置或教练表读取
     * 
     * 3. 预计总收入 = 基本薪资 + 提成
     *    - 基本薪资从教练表salary字段读取
     *    - 若未设置基本薪资则默认为0
     * </pre>
     * 
     * <h4>为什么默认提成比例是10%：</h4>
     * <p>
     * 10%是健身行业常见的教练课程提成比例，既能激励教练，
     * 又能保证俱乐部的运营利润。实际使用中可根据业务调整。
     * </p>
     * 
     * @param coachId 教练ID
     * @param startDate 开始日期（包含），格式：yyyy-MM-dd
     * @param endDate 结束日期（包含），格式：yyyy-MM-dd
     * @return 业绩统计数据，包含courseCount、totalStudents、totalIncome、commission等
     */
    @Override
    public Map<String, Object> getCoachPerformance(Long coachId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取教练信息（用于读取基本薪资）
        Coach coach = coachMapper.selectById(coachId);
        
        // ========== 1. 查询指定时间范围内的课程 ==========
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCoachId, coachId)
                        .ge(Course::getStartTime, startDate)
                        .le(Course::getStartTime, endDate));
        
        result.put("courseCount", courses.size());
        
        // ========== 2. 统计学员数和课程收入 ==========
        int totalStudents = 0;
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Course course : courses) {
            // 累加每节课的实际报名人数
            totalStudents += course.getCurrentCount();
            // 课程收入 = 课程价格 × 实际报名人数
            totalIncome = totalIncome.add(course.getPrice().multiply(BigDecimal.valueOf(course.getCurrentCount())));
        }
        result.put("totalStudents", totalStudents);
        result.put("totalIncome", totalIncome);
        
        // ========== 3. 计算提成 ==========
        // 提成比例固定为10%（后续可改为从配置或教练表读取）
        BigDecimal commissionRate = new BigDecimal("0.10");
        // 提成 = 课程总收入 × 提成比例
        BigDecimal commission = totalIncome.multiply(commissionRate);
        result.put("commissionRate", commissionRate.multiply(new BigDecimal("100")).intValue() + "%");
        result.put("commission", commission);
        
        // ========== 4. 计算预计总收入 ==========
        // 基本薪资，若未设置则默认为0
        BigDecimal baseSalary = coach != null && coach.getSalary() != null ? coach.getSalary() : BigDecimal.ZERO;
        result.put("baseSalary", baseSalary);
        
        // 预计总收入 = 基本薪资 + 提成
        result.put("expectedIncome", baseSalary.add(commission));
        
        return result;
    }

    /**
     * 获取会员个人统计数据
     * <p>
     * 统计会员的课程预约情况和会员资格状态。
     * </p>
     * 
     * <h4>到期提醒逻辑：</h4>
     * <pre>
     * daysUntilExpire = expireDate - today
     * 
     * isExpired = daysUntilExpire < 0  // 已过期
     * isExpiringSoon = 0 <= daysUntilExpire <= 7  // 7天内到期
     * </pre>
     * 
     * <h4>为什么到期提醒设为7天：</h4>
     * <p>
     * 7天是一个合理的提醒周期，给会员足够的时间考虑续费，
     * 同时也不会过早打扰用户。这个值可以根据业务需求调整。
     * </p>
     * 
     * <h4>统计字段说明：</h4>
     * <ul>
     *   <li>reservationCount：总预约次数（包含所有状态）</li>
     *   <li>completedCount：已完成课程数（status=1，已签到）</li>
     *   <li>expireDate：会员到期日期</li>
     *   <li>daysUntilExpire：距离到期的天数（负数表示已过期）</li>
     *   <li>isExpired：是否已过期</li>
     *   <li>isExpiringSoon：是否即将到期（7天内）</li>
     *   <li>balance：账户余额</li>
     *   <li>level：会员等级</li>
     * </ul>
     * 
     * @param memberId 会员ID
     * @return 会员统计数据
     */
    @Override
    public Map<String, Object> getMemberStatistics(Long memberId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取会员信息
        Member member = memberMapper.selectById(memberId);
        
        // ========== 1. 统计预约课程数 ==========
        // 总预约数（包含所有状态的预约）
        result.put("reservationCount", reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getMemberId, memberId)));
        
        // 已完成课程数（status=1 表示已签到）
        result.put("completedCount", reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getMemberId, memberId)
                        .eq(Reservation::getStatus, 1)));  // 1-已签到
        
        // ========== 2. 会员到期提醒 ==========
        if (member != null && member.getExpireDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate expireDate = member.getExpireDate();
            
            // 计算距离到期的天数
            // ChronoUnit.DAYS.between(a, b) 返回 b - a 的天数
            // 正数表示未到期，负数表示已过期
            long daysUntilExpire = java.time.temporal.ChronoUnit.DAYS.between(today, expireDate);
            
            result.put("expireDate", expireDate);
            result.put("daysUntilExpire", daysUntilExpire);
            result.put("isExpired", daysUntilExpire < 0);  // 已过期
            // 7天内到期视为即将到期，前端可据此显示提醒
            result.put("isExpiringSoon", daysUntilExpire >= 0 && daysUntilExpire <= 7);
        }
        
        // ========== 3. 会员余额和等级 ==========
        if (member != null) {
            result.put("balance", member.getBalance());
            result.put("level", member.getLevel());  // 1-普通 2-银卡 3-金卡 4-钻石
        }
        
        return result;
    }

    @Override
    public List<HighRiskCourseVO> getHighRiskCourses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .ge(Course::getStartTime, now)
                        .le(Course::getStartTime, next24h)
                        .eq(Course::getStatus, 1));

        List<HighRiskCourseVO> result = new ArrayList<>();
        for (Course course : courses) {
            HighRiskCourseVO vo = calculateCourseRisk(course);
            if (!"LOW".equals(vo.getRiskLevel()) || vo.getWaitingCount() > 0) {
                result.add(vo);
            }
        }

        result.sort((a, b) -> {
            int orderA = "HIGH".equals(a.getRiskLevel()) ? 3 : "MEDIUM".equals(a.getRiskLevel()) ? 2 : 1;
            int orderB = "HIGH".equals(b.getRiskLevel()) ? 3 : "MEDIUM".equals(b.getRiskLevel()) ? 2 : 1;
            return orderB - orderA;
        });
        return result;
    }

    private HighRiskCourseVO calculateCourseRisk(Course course) {
        HighRiskCourseVO vo = new HighRiskCourseVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getName());
        vo.setCoachName(course.getCoachName());
        vo.setStartTime(course.getStartTime());
        vo.setEndTime(course.getEndTime());
        vo.setMaxCapacity(course.getMaxCapacity());
        vo.setCurrentCount(course.getCurrentCount());

        int waitingCount = baseMapperWait.count(new LambdaQueryWrapper<WaitingList>()
                .eq(WaitingList::getCourseId, course.getId())
                .eq(WaitingList::getStatus, 0));
        vo.setWaitingCount(waitingCount);

        int totalReservations = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getCourseId, course.getId())
                        .ne(Reservation::getStatus, 2)).intValue();

        int absentCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getCourseId, course.getId())
                        .eq(Reservation::getStatus, 3)).intValue();

        BigDecimal noShowRate = BigDecimal.ZERO;
        if (totalReservations > 0) {
            noShowRate = new BigDecimal(absentCount)
                    .divide(new BigDecimal(totalReservations), 4, RoundingMode.HALF_UP);
        }
        vo.setNoShowRate(noShowRate);

        double rate = noShowRate.doubleValue();
        String riskLevel = "LOW";
        if (rate >= 0.3 && waitingCount > 0) {
            riskLevel = "HIGH";
        } else if (rate >= 0.15 && waitingCount > 0) {
            riskLevel = "MEDIUM";
        }
        vo.setRiskLevel(riskLevel);

        return vo;
    }

    @org.springframework.beans.factory.annotation.Autowired
    private WaitingListMapper baseMapperWait;
}
