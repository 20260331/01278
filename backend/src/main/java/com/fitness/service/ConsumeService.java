package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.ConsumeRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费记录Service
 */
public interface ConsumeService extends IService<ConsumeRecord> {

    /**
     * 分页查询消费记录
     */
    PageResult<ConsumeRecord> pageList(Page<ConsumeRecord> page, Long memberId, Integer type);

    /**
     * 会员充值
     */
    void recharge(Long memberId, BigDecimal amount, String description);

    /**
     * 收支统计
     */
    Map<String, Object> getStatistics(String startDate, String endDate);

    /**
     * 获取会员消费记录列表（用于导出）
     */
    List<ConsumeRecord> getListForExport(Long memberId, Integer type, String startDate, String endDate);
}
