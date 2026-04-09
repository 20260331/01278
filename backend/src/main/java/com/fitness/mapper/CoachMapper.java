package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Coach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 教练Mapper
 */
@Mapper
public interface CoachMapper extends BaseMapper<Coach> {

    /**
     * 根据用户ID查询教练
     */
    @Select("SELECT c.*, u.username FROM coach c LEFT JOIN sys_user u ON c.user_id = u.id WHERE c.user_id = #{userId}")
    Coach selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询教练（带用户名）
     */
    IPage<Coach> selectPageWithUsername(Page<Coach> page, @Param("name") String name, @Param("status") Integer status);
}
