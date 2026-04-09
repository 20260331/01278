package com.fitness.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.ConsumeRecord;
import com.fitness.entity.Member;
import com.fitness.service.ConsumeService;
import com.fitness.service.MemberService;
import com.fitness.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 消费记录控制器
 * <p>
 * 管理会员的消费记录，包括：
 * - 消费记录查询
 * - 会员充值
 * - 收支统计
 * - 记录导出
 * </p>
 * 
 * <h3>消费类型说明：</h3>
 * <ul>
 *   <li>1 - 充值：会员账户充值</li>
 *   <li>2 - 消费：购买课程、商品等消费</li>
 *   <li>3 - 退款：退款操作</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "消费管理", description = "消费记录查询、充值、统计、导出等接口")
@RestController
@RequestMapping("/api/v1/consume-records")
@RequiredArgsConstructor
public class ConsumeController {

    private final ConsumeService consumeService;
    private final MemberService memberService;

    /**
     * 分页查询消费记录
     */
    @Operation(summary = "分页查询消费记录", description = "根据条件分页查询消费记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public Result<PageResult<ConsumeRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer type) {
        return Result.success(consumeService.pageList(new Page<>(current, size), memberId, type));
    }

    /**
     * 获取我的消费记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<PageResult<ConsumeRecord>> myRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        return Result.success(consumeService.pageList(new Page<>(current, size), member.getId(), type));
    }

    /**
     * 会员充值
     */
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> recharge(@RequestBody Map<String, Object> params) {
        Long memberId = Long.valueOf(params.get("memberId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String description = (String) params.get("description");
        consumeService.recharge(memberId, amount, description);
        return Result.success();
    }

    /**
     * 收支统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> statistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(consumeService.getStatistics(startDate, endDate));
    }

    /**
     * 导出我的消费记录
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('MEMBER')")
    public void exportMyRecords(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberService.getByUserId(userId);
        List<ConsumeRecord> list = consumeService.getListForExport(member.getId(), type, startDate, endDate);
        
        exportToExcel(list, response, "我的消费记录");
    }

    /**
     * 导出消费记录（管理员）
     */
    @GetMapping("/export/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportRecords(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        List<ConsumeRecord> list = consumeService.getListForExport(memberId, type, startDate, endDate);
        exportToExcel(list, response, "消费记录");
    }

    /**
     * 导出Excel公共方法
     */
    private void exportToExcel(List<ConsumeRecord> list, HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8.name()));

            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.addHeaderAlias("id", "记录ID");
            writer.addHeaderAlias("memberName", "会员姓名");
            writer.addHeaderAlias("type", "类型");
            writer.addHeaderAlias("amount", "金额");
            writer.addHeaderAlias("balanceAfter", "余额");
            writer.addHeaderAlias("description", "描述");
            writer.addHeaderAlias("createTime", "时间");

            // 转换类型显示
            for (ConsumeRecord record : list) {
                if (record.getType() != null) {
                    record.setDescription(record.getDescription() + " [" + getTypeText(record.getType()) + "]");
                }
            }

            writer.setOnlyAlias(true);
            writer.write(list, true);

            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    private String getTypeText(Integer type) {
        switch (type) {
            case 1: return "充值";
            case 2: return "消费";
            case 3: return "退款";
            default: return "其他";
        }
    }
}
