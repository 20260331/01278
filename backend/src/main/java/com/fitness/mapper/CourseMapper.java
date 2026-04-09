package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程（带教练名）
     */
    IPage<Course> selectPageWithCoach(Page<Course> page, @Param("name") String name, @Param("type") String type, 
                                       @Param("coachId") Long coachId, @Param("status") Integer status);
}
