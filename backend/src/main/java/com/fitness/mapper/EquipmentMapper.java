package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 器材Mapper
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
}
