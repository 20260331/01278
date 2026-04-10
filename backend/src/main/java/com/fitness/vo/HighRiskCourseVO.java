package com.fitness.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 高风险课程VO
 */
@Data
public class HighRiskCourseVO {
    private Long courseId;
    private String courseName;
    private String courseType;
    private String coachName;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    private Integer maxCapacity;
    private Integer currentCount;
    private Integer waitlistCount;
    private Integer availableSeats;

    private BigDecimal noShowRate;
    private Integer riskLevel;
    private String riskReason;
}
