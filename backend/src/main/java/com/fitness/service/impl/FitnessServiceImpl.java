package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.FitnessRecord;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.FitnessRecordMapper;
import com.fitness.service.FitnessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健身记录Service实现
 */
@Slf4j
@Service
public class FitnessServiceImpl extends ServiceImpl<FitnessRecordMapper, FitnessRecord> implements FitnessService {

    @Override
    public PageResult<FitnessRecord> pageList(Page<FitnessRecord> page, Long memberId) {
        return PageResult.of(baseMapper.selectPageWithMember(page, memberId));
    }

    @Override
    public void createRecord(FitnessRecord record) {
        save(record);
        log.info("录入健身记录: memberId={}", record.getMemberId());
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long memberId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最近7天的记录
        List<FitnessRecord> records = baseMapper.selectRecentByMemberId(memberId, 7);
        
        // 统计
        int totalDuration = 0;
        int totalCalories = 0;
        BigDecimal startWeight = null;
        BigDecimal endWeight = null;
        
        for (FitnessRecord record : records) {
            if (record.getDuration() != null) {
                totalDuration += record.getDuration();
            }
            if (record.getCalories() != null) {
                totalCalories += record.getCalories();
            }
            if (record.getWeight() != null) {
                if (startWeight == null) {
                    startWeight = record.getWeight();
                }
                endWeight = record.getWeight();
            }
        }
        
        result.put("records", records);
        result.put("trainDays", records.size());
        result.put("totalDuration", totalDuration);
        result.put("avgDuration", records.isEmpty() ? 0 : totalDuration / records.size());
        result.put("totalCalories", totalCalories);
        
        if (startWeight != null && endWeight != null) {
            result.put("weightChange", endWeight.subtract(startWeight).setScale(2, RoundingMode.HALF_UP));
        } else {
            result.put("weightChange", BigDecimal.ZERO);
        }
        
        return result;
    }

    @Override
    public void sendAdvice(Long recordId, String advice, Long coachId) {
        FitnessRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("健身记录不存在");
        }
        
        record.setCoachAdvice(advice);
        record.setAdviceCoachId(coachId);
        record.setAdviceTime(LocalDateTime.now());
        updateById(record);
        
        log.info("教练发送健身建议: recordId={}, coachId={}", recordId, coachId);
    }
}
