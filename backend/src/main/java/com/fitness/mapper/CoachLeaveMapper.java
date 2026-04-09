package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.CoachLeave;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 教练请假/调课Mapper
 */
@Mapper
public interface CoachLeaveMapper extends BaseMapper<CoachLeave> {

    /**
     * 分页查询请假申请（带关联信息）
     */
    IPage<CoachLeave> selectPageWithDetail(Page<CoachLeave> page, @Param("coachId") Long coachId, @Param("status") Integer status);
}
