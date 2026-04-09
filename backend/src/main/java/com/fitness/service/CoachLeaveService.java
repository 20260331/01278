package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.CoachLeave;

/**
 * 教练请假/调课Service
 */
public interface CoachLeaveService extends IService<CoachLeave> {

    /**
     * 分页查询请假申请
     */
    PageResult<CoachLeave> pageList(Page<CoachLeave> page, Long coachId, Integer status);

    /**
     * 提交请假/调课申请
     */
    void createLeave(CoachLeave leave);

    /**
     * 审核申请
     */
    void audit(Long id, Integer status, String remark);

    /**
     * 检查教练在指定时间段是否请假
     * @param coachId 教练ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 如果请假返回请假记录，否则返回null
     */
    CoachLeave checkLeave(Long coachId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
