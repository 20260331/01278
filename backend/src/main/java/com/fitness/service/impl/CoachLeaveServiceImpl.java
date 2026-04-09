package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.CoachLeave;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.CoachLeaveMapper;
import com.fitness.service.CoachLeaveService;
import com.fitness.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 教练请假/调课Service实现
 * <p>
 * 处理教练请假和调课申请的业务逻辑。
 * </p>
 * 
 * <h3>申请状态说明：</h3>
 * <ul>
 *   <li>0 - 待审核：申请已提交，等待管理员审核</li>
 *   <li>1 - 已通过：申请已被管理员批准</li>
 *   <li>2 - 已拒绝：申请被管理员拒绝</li>
 * </ul>
 * 
 * <h3>申请类型说明：</h3>
 * <ul>
 *   <li>1 - 请假：教练请假，该时间段内不安排课程</li>
 *   <li>2 - 调课：教练调整课程时间</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CoachLeaveServiceImpl extends ServiceImpl<CoachLeaveMapper, CoachLeave> implements CoachLeaveService {

    @Override
    public PageResult<CoachLeave> pageList(Page<CoachLeave> page, Long coachId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithDetail(page, coachId, status));
    }

    @Override
    public void createLeave(CoachLeave leave) {
        leave.setStatus(0);
        save(leave);
        log.info("提交请假申请: coachId={}, type={}", leave.getCoachId(), leave.getType());
    }

    @Override
    public void audit(Long id, Integer status, String remark) {
        CoachLeave leave = getById(id);
        if (leave == null) {
            throw new BusinessException("申请不存在");
        }
        if (leave.getStatus() != 0) {
            throw new BusinessException("该申请已审核");
        }
        
        leave.setStatus(status);
        leave.setAuditTime(LocalDateTime.now());
        leave.setAuditUserId(SecurityUtil.getCurrentUserId());
        leave.setAuditRemark(remark);
        updateById(leave);
        
        log.info("审核请假申请: id={}, status={}", id, status);
    }

    /**
     * 检查教练在指定时间段是否有请假
     * <p>
     * 用于在安排课程前检查教练是否有请假冲突。
     * 只检查已批准(status=1)的请假记录。
     * </p>
     * 
     * <h4>时间重叠判断逻辑：</h4>
     * <p>两个时间段存在重叠的情况有三种：</p>
     * <pre>
     * 情况1：课程开始时间在请假时间段内
     *        请假: |---------|
     *        课程:     |------|
     *               ↑课程开始时间在请假时间段内
     * 
     * 情况2：课程结束时间在请假时间段内
     *        请假:     |---------|
     *        课程: |------|
     *                    ↑课程结束时间在请假时间段内
     * 
     * 情况3：课程时间段完全包含请假时间段
     *        请假:   |---|
     *        课程: |-------|
     *              ↑请假开始时间在课程时间段内
     * </pre>
     * 
     * <h4>SQL条件解释：</h4>
     * <pre>
     * WHERE coach_id = ? 
     *   AND status = 1                          -- 只检查已批准的请假
     *   AND (
     *     startTime BETWEEN start_time AND end_time  -- 情况1
     *     OR endTime BETWEEN start_time AND end_time -- 情况2
     *     OR start_time BETWEEN startTime AND endTime -- 情况3
     *   )
     * LIMIT 1  -- 只需要找到一条冲突记录即可
     * </pre>
     * 
     * @param coachId 教练ID
     * @param startTime 课程开始时间
     * @param endTime 课程结束时间
     * @return 冲突的请假记录，如果没有冲突则返回null
     */
    @Override
    public CoachLeave checkLeave(Long coachId, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询已批准的请假记录，检查时间是否有重叠
        return lambdaQuery()
                .eq(CoachLeave::getCoachId, coachId)
                .eq(CoachLeave::getStatus, 1)  // 只检查已批准(status=1)的请假
                .and(w -> w
                        // 情况1：课程开始时间在请假时间段内
                        .apply("({0} BETWEEN start_time AND end_time)", startTime)
                        // 情况2：课程结束时间在请假时间段内
                        .or()
                        .apply("({0} BETWEEN start_time AND end_time)", endTime)
                        // 情况3：课程时间段包含请假时间段（请假开始时间在课程时间段内）
                        .or()
                        .apply("(start_time BETWEEN {0} AND {1})", startTime, endTime)
                )
                .last("LIMIT 1")  // 只需要找到一条冲突记录即可
                .one();
    }
}
