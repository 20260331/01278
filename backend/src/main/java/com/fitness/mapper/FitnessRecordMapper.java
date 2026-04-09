package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.FitnessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 健身记录Mapper
 */
@Mapper
public interface FitnessRecordMapper extends BaseMapper<FitnessRecord> {

    /**
     * 分页查询健身记录
     */
    IPage<FitnessRecord> selectPageWithMember(Page<FitnessRecord> page, @Param("memberId") Long memberId);

    /**
     * 查询会员最近N天的健身记录
     */
    List<FitnessRecord> selectRecentByMemberId(@Param("memberId") Long memberId, @Param("days") Integer days);
}
