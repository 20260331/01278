package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 器材实体
 */
@Data
@TableName("equipment")
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String brand;

    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastMaintainDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintainDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
