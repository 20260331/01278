package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录实体
 */
@Data
@TableName("consume_record")
public class ConsumeRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Integer type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String description;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String memberName;

    @TableField(exist = false)
    private String operatorName;
}
