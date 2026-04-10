package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.WaitingQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaitingQueueMapper extends BaseMapper<WaitingQueue> {

    List<WaitingQueue> selectWaitingQueueWithDetail(@Param("courseId") Long courseId, @Param("status") Integer status);

    @Select("SELECT COALESCE(MAX(queue_order), 0) + 1 FROM waiting_queue WHERE course_id = #{courseId} AND status = 0")
    Integer getNextQueueOrder(Long courseId);

    @Select("SELECT COUNT(*) FROM reservation r " +
            "JOIN course c ON r.course_id = c.id " +
            "WHERE r.member_id = #{memberId} " +
            "AND r.status = 3 " +
            "AND c.start_time >= DATE_SUB(NOW(), INTERVAL 3 MONTH)")
    Integer getMemberMissCount(Long memberId);

    @Select("SELECT COUNT(*) FROM reservation r " +
            "JOIN course c ON r.course_id = c.id " +
            "WHERE r.member_id = #{memberId} " +
            "AND r.status IN (0, 1, 3) " +
            "AND c.start_time >= DATE_SUB(NOW(), INTERVAL 3 MONTH)")
    Integer getMemberTotalReservationCount(Long memberId);

    List<WaitingQueue> selectWaitingQueueWithMember(@Param("memberId") Long memberId);

    List<WaitingQueue> selectUnreadNotified(@Param("memberId") Long memberId);
}
