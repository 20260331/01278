package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.WaitingList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaitingListMapper extends BaseMapper<WaitingList> {

    Page<WaitingList> selectPageWithDetail(Page<WaitingList> page,
                                           @Param("memberId") Long memberId,
                                           @Param("courseId") Long courseId,
                                           @Param("status") Integer status);

    List<WaitingList> selectByCourseIdOrderByQueueOrder(@Param("courseId") Long courseId);

    @Select("SELECT COALESCE(MAX(queue_order), 0) + 1 FROM waiting_list WHERE course_id = #{courseId} AND status = 0")
    Integer getNextQueueOrder(Long courseId);
}
