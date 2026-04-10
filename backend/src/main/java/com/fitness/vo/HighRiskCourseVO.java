package com.fitness.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HighRiskCourseVO {
    private Long courseId;
    private String courseName;
    private String coachName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    private Integer maxCapacity;
    private Integer currentCount;
    private Integer waitingCount;
    private BigDecimal noShowRate;
    private String riskLevel;
    private LocalDateTime endTime;
}
