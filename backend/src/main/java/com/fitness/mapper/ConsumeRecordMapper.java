package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.ConsumeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 消费记录Mapper
 */
@Mapper
public interface ConsumeRecordMapper extends BaseMapper<ConsumeRecord> {

    /**
     * 分页查询消费记录（带关联信息）
     */
    IPage<ConsumeRecord> selectPageWithMember(Page<ConsumeRecord> page, @Param("memberId") Long memberId, @Param("type") Integer type);

    /**
     * 查询消费记录列表（用于导出）
     */
    List<ConsumeRecord> selectListForExport(@Param("memberId") Long memberId, @Param("type") Integer type, 
                                             @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计收入
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM consume_record WHERE amount > 0 AND DATE(create_time) BETWEEN #{startDate} AND #{endDate}")
    BigDecimal sumIncome(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计支出
     */
    @Select("SELECT COALESCE(SUM(ABS(amount)), 0) FROM consume_record WHERE amount < 0 AND DATE(create_time) BETWEEN #{startDate} AND #{endDate}")
    BigDecimal sumExpense(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
