package com.fitness.service;

import java.util.Map;

/**
 * 数据备份服务接口
 * <p>
 * 提供数据备份和恢复功能
 * </p>
 *
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
public interface BackupService {

    /**
     * 导出备份数据
     *
     * @return 备份数据Map
     */
    Map<String, Object> exportBackupData();

    /**
     * 获取备份统计信息
     *
     * @return 各表记录数统计
     */
    Map<String, Object> getBackupStats();

    /**
     * 验证备份文件
     *
     * @param backupData 备份数据
     * @return 验证结果
     */
    Map<String, Object> validateBackup(Map<String, Object> backupData);

    /**
     * 执行数据恢复
     * <p>
     * 支持两种模式：
     * - 增量导入：仅导入ID不存在的记录
     * - 覆盖导入：先清空表再导入（危险操作，需要确认）
     * </p>
     *
     * @param backupData 备份数据
     * @param mode       恢复模式：increment（增量）或 overwrite（覆盖）
     * @return 恢复结果统计
     */
    Map<String, Object> restoreData(Map<String, Object> backupData, String mode);
}
