package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健身记录实体
 */
@Data
@TableName("fitness_record")
public class FitnessRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    private BigDecimal weight;

    private BigDecimal height;

    private Integer duration;

    private Integer calories;

    private String content;

    private String remark;

    private String coachAdvice;

    private Long adviceCoachId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime adviceTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String memberName;

    @TableField(exist = false)
    private String adviceCoachName;
}
