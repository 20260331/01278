package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 预约Mapper
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    /**
     * 分页查询预约记录（带关联信息）
     */
    IPage<Reservation> selectPageWithDetail(Page<Reservation> page, @Param("memberId") Long memberId,
                                             @Param("courseId") Long courseId, @Param("status") Integer status);
}
