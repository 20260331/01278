package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.CourseWaitlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程候补Mapper
 */
@Mapper
public interface CourseWaitlistMapper extends BaseMapper<CourseWaitlist> {

    /**
     * 分页查询候补记录（带关联信息）
     */
    IPage<CourseWaitlist> selectPageWithDetail(Page<CourseWaitlist> page, @Param("memberId") Long memberId,
                                                @Param("courseId") Long courseId, @Param("status") Integer status);

    /**
     * 获取课程当前最大排队序号
     */
    Integer selectMaxQueueNo(@Param("courseId") Long courseId);

    /**
     * 获取课程的候补列表（按排队顺序）
     */
    List<CourseWaitlist> selectWaitlistByCourseId(@Param("courseId") Long courseId, @Param("status") Integer status);

    /**
     * 获取会员在课程中的候补记录
     */
    CourseWaitlist selectByMemberAndCourse(@Param("memberId") Long memberId, @Param("courseId") Long courseId);

    /**
     * 获取课程候补人数
     */
    Integer selectWaitlistCount(@Param("courseId") Long courseId);
}
