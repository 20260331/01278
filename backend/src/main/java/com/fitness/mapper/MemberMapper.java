package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员Mapper
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
    
    /**
     * 根据用户ID查询会员
     */
    @Select("SELECT m.*, u.username FROM member m LEFT JOIN sys_user u ON m.user_id = u.id WHERE m.user_id = #{userId}")
    Member selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询会员（带用户名）
     */
    IPage<Member> selectPageWithUsername(Page<Member> page, @Param("name") String name, @Param("phone") String phone, @Param("status") Integer status);
}
