package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 反馈Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 分页查询反馈（带会员名）
     */
    IPage<Feedback> selectPageWithMember(Page<Feedback> page, @Param("memberId") Long memberId, @Param("status") Integer status);
}
