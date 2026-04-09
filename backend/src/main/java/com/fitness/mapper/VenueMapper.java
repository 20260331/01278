package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.Venue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 场地Mapper
 */
@Mapper
public interface VenueMapper extends BaseMapper<Venue> {
}
