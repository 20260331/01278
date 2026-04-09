package com.fitness.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.Result;
import com.fitness.entity.Coach;
import com.fitness.entity.Course;
import com.fitness.mapper.CoachMapper;
import com.fitness.mapper.CourseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪资结算控制器
 * <p>
 * 管理教练薪资结算，包括：
 * - 教练薪资列表查询
 * - 薪资汇总统计
 * </p>
 * 
 * <h3>薪资计算规则：</h3>
 * <ul>
 *   <li>基本薪资：教练设定的月基本工资</li>
 *   <li>提成比例：课程收入的10%</li>
 *   <li>总薪资 = 基本薪资 + 课程收入 × 提成比例</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "薪资管理", description = "教练薪资结算、汇总统计等接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/salaries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SalaryController {

    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;

    /**
     * 获取教练薪资列表
     */
    @Operation(summary = "获取教练薪资列表", description = "获取指定月份所有教练的薪资明细")
    @GetMapping
    public Result<List<Map<String, Object>>> getSalaryList(
            @Parameter(description = "月份，格式：yyyy-MM") @RequestParam String month) {
        
        // 解析月份
        LocalDate monthDate = LocalDate.parse(month + "-01");
        LocalDateTime startTime = monthDate.atStartOfDay();
        LocalDateTime endTime = monthDate.plusMonths(1).atStartOfDay();
        
        // 获取所有正常状态的教练
        List<Coach> coaches = coachMapper.selectList(
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Coach coach : coaches) {
            Map<String, Object> salaryInfo = new HashMap<>();
            salaryInfo.put("coachId", coach.getId());
            salaryInfo.put("coachName", coach.getName());
            salaryInfo.put("baseSalary", coach.getSalary() != null ? coach.getSalary() : BigDecimal.ZERO);
            
            // 查询该月课程
            List<Course> courses = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>()
                            .eq(Course::getCoachId, coach.getId())
                            .ge(Course::getStartTime, startTime)
                            .lt(Course::getStartTime, endTime));
            
            int courseCount = courses.size();
            int studentCount = 0;
            BigDecimal courseIncome = BigDecimal.ZERO;
            
            for (Course course : courses) {
                studentCount += course.getCurrentCount();
                courseIncome = courseIncome.add(
                        course.getPrice().multiply(BigDecimal.valueOf(course.getCurrentCount())));
            }
            
            // 提成计算（10%）
            BigDecimal commissionRate = new BigDecimal("0.10");
            BigDecimal commission = courseIncome.multiply(commissionRate);
            
            salaryInfo.put("courseCount", courseCount);
            salaryInfo.put("studentCount", studentCount);
            salaryInfo.put("courseIncome", courseIncome);
            salaryInfo.put("commissionRate", "10%");
            salaryInfo.put("commission", commission);
            
            // 总薪资
            BigDecimal baseSalary = coach.getSalary() != null ? coach.getSalary() : BigDecimal.ZERO;
            salaryInfo.put("totalSalary", baseSalary.add(commission));
            
            result.add(salaryInfo);
        }
        
        return Result.success(result);
    }

    /**
     * 获取薪资汇总
     */
    @Operation(summary = "获取薪资汇总", description = "获取指定月份的薪资汇总统计")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSalarySummary(
            @Parameter(description = "月份，格式：yyyy-MM") @RequestParam String month) {
        
        LocalDate monthDate = LocalDate.parse(month + "-01");
        LocalDateTime startTime = monthDate.atStartOfDay();
        LocalDateTime endTime = monthDate.plusMonths(1).atStartOfDay();
        
        List<Coach> coaches = coachMapper.selectList(
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1));
        
        BigDecimal totalBaseSalary = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        int totalCourses = 0;
        int totalStudents = 0;
        
        for (Coach coach : coaches) {
            totalBaseSalary = totalBaseSalary.add(
                    coach.getSalary() != null ? coach.getSalary() : BigDecimal.ZERO);
            
            List<Course> courses = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>()
                            .eq(Course::getCoachId, coach.getId())
                            .ge(Course::getStartTime, startTime)
                            .lt(Course::getStartTime, endTime));
            
            for (Course course : courses) {
                totalCourses++;
                totalStudents += course.getCurrentCount();
                BigDecimal income = course.getPrice().multiply(BigDecimal.valueOf(course.getCurrentCount()));
                totalCommission = totalCommission.add(income.multiply(new BigDecimal("0.10")));
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("coachCount", coaches.size());
        summary.put("totalBaseSalary", totalBaseSalary);
        summary.put("totalCommission", totalCommission);
        summary.put("totalSalary", totalBaseSalary.add(totalCommission));
        summary.put("totalCourses", totalCourses);
        summary.put("totalStudents", totalStudents);
        summary.put("month", month);
        
        return Result.success(summary);
    }
}
