package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.FitnessRecord;

import java.util.Map;

/**
 * 健身记录Service
 */
public interface FitnessService extends IService<FitnessRecord> {

    /**
     * 分页查询健身记录
     */
    PageResult<FitnessRecord> pageList(Page<FitnessRecord> page, Long memberId);

    /**
     * 录入健身数据
     */
    void createRecord(FitnessRecord record);

    /**
     * 生成健身周报
     */
    Map<String, Object> getWeeklyReport(Long memberId);

    /**
     * 教练发送健身建议
     */
    void sendAdvice(Long recordId, String advice, Long coachId);
}
