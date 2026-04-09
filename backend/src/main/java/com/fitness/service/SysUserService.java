package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.PasswordDTO;
import com.fitness.entity.SysUser;
import com.fitness.vo.LoginVO;

/**
 * 系统用户Service
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    LoginVO getCurrentUser();

    /**
     * 修改密码
     */
    void updatePassword(PasswordDTO dto);

    /**
     * 分页查询用户
     */
    PageResult<SysUser> pageList(Page<SysUser> page, String username, String role);

    /**
     * 创建用户
     */
    void createUser(SysUser user);

    /**
     * 更新用户
     */
    void updateUser(SysUser user);

    /**
     * 管理员重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
}
