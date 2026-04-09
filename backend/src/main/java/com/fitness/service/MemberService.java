package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Member;

/**
 * 会员Service
 */
public interface MemberService extends IService<Member> {

    /**
     * 分页查询会员
     */
    PageResult<Member> pageList(Page<Member> page, String name, String phone, Integer status);

    /**
     * 根据用户ID获取会员
     */
    Member getByUserId(Long userId);

    /**
     * 创建会员（同时创建用户账号）
     */
    void createMember(Member member, String username, String password);

    /**
     * 更新会员信息
     */
    void updateMember(Member member);

    /**
     * 修改会员状态（冻结/激活）
     */
    void updateStatus(Long id, Integer status);
}
