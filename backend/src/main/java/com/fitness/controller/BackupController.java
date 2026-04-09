package com.fitness.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.fitness.common.Result;
import com.fitness.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 数据备份控制器
 * <p>
 * 管理系统数据备份与恢复，包括：
 * - 数据导出备份
 * - 备份统计信息
 * - 数据导入恢复（支持增量和覆盖模式）
 * </p>
 * 
 * <h3>备份内容：</h3>
 * <ul>
 *   <li>会员数据</li>
 *   <li>教练数据</li>
 *   <li>课程数据</li>
 *   <li>器材数据</li>
 *   <li>反馈数据</li>
 *   <li>消费记录</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "数据备份", description = "系统数据备份与恢复接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/backups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    /**
     * 导出数据备份
     */
    @Operation(summary = "导出数据备份", description = "将系统数据导出为JSON备份文件")
    @GetMapping("/export")
    public void exportBackup(HttpServletResponse response) {
        try {
            Map<String, Object> backupData = backupService.exportBackupData();
            
            String json = JSONUtil.toJsonPrettyStr(backupData);
            
            String fileName = "fitness_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            response.setContentType("application/json;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
            
            ServletOutputStream out = response.getOutputStream();
            out.write(json.getBytes(StandardCharsets.UTF_8));
            IoUtil.close(out);
            
            log.info("数据备份导出成功");
        } catch (Exception e) {
            log.error("数据备份导出失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份统计信息
     */
    @Operation(summary = "获取备份统计信息", description = "获取各数据表的记录数量统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBackupStats() {
        return Result.success(backupService.getBackupStats());
    }

    /**
     * 验证备份文件
     */
    @Operation(summary = "验证备份文件", description = "验证备份文件格式和完整性，返回统计信息")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateBackup(@RequestParam("file") MultipartFile file) {
        try {
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> backupData = JSONUtil.toBean(json, Map.class);
            
            Map<String, Object> result = backupService.validateBackup(backupData);
            result.put("message", "备份文件验证成功，包含 " + result.get("totalRecords") + " 条记录");
            
            log.info("备份文件验证成功: {}", result);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("备份文件解析失败", e);
            return Result.error("备份文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 导入数据恢复
     * <p>
     * 支持两种模式：
     * - increment（增量）：仅导入ID不存在的记录，跳过已存在的记录
     * - overwrite（覆盖）：更新已存在的记录，导入不存在的记录
     * </p>
     */
    @Operation(summary = "导入数据恢复", description = "从JSON备份文件恢复数据，支持增量和覆盖两种模式")
    @PostMapping("/import")
    public Result<Map<String, Object>> importBackup(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "恢复模式：increment（增量，默认）或 overwrite（覆盖）")
            @RequestParam(value = "mode", defaultValue = "increment") String mode) {
        try {
            // 验证恢复模式
            if (!"increment".equals(mode) && !"overwrite".equals(mode)) {
                return Result.error("无效的恢复模式，请使用 increment 或 overwrite");
            }
            
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> backupData = JSONUtil.toBean(json, Map.class);
            
            // 先验证备份文件
            Map<String, Object> validation = backupService.validateBackup(backupData);
            if (!(Boolean) validation.getOrDefault("valid", false)) {
                return Result.error("备份文件验证失败");
            }
            
            // 执行数据恢复
            Map<String, Object> result = backupService.restoreData(backupData, mode);
            
            log.info("数据恢复完成: {}", result);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("数据恢复失败", e);
            return Result.error("数据恢复失败: " + e.getMessage());
        }
    }
}
