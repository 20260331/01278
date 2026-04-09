package com.fitness.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.ConsumeRecord;
import com.fitness.entity.Member;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.ConsumeRecordMapper;
import com.fitness.mapper.MemberMapper;
import com.fitness.service.ConsumeService;
import com.fitness.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeServiceImpl extends ServiceImpl<ConsumeRecordMapper, ConsumeRecord> implements ConsumeService {

    private final MemberMapper memberMapper;

    @Override
    public PageResult<ConsumeRecord> pageList(Page<ConsumeRecord> page, Long memberId, Integer type) {
        return PageResult.of(baseMapper.selectPageWithMember(page, memberId, type));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long memberId, BigDecimal amount, String description) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        
        // 更新余额
        BigDecimal newBalance = member.getBalance().add(amount);
        member.setBalance(newBalance);
        memberMapper.updateById(member);
        
        // 创建消费记录
        ConsumeRecord record = new ConsumeRecord();
        record.setMemberId(memberId);
        record.setType(1); // 充值
        record.setAmount(amount);
        record.setBalanceAfter(newBalance);
        record.setDescription(description != null ? description : "会员充值");
        record.setOperatorId(SecurityUtil.getCurrentUserId());
        save(record);
        
        log.info("会员充值: memberId={}, amount={}", memberId, amount);
    }

    @Override
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal income = baseMapper.sumIncome(startDate, endDate);
        BigDecimal expense = baseMapper.sumExpense(startDate, endDate);
        
        result.put("income", income);
        result.put("expense", expense);
        result.put("profit", income.subtract(expense));
        
        return result;
    }

    @Override
    public List<ConsumeRecord> getListForExport(Long memberId, Integer type, String startDate, String endDate) {
        return baseMapper.selectListForExport(memberId, type, startDate, endDate);
    }
}
