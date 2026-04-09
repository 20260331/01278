package com.fitness.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.fitness.entity.*;
import com.fitness.mapper.*;
import com.fitness.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据备份服务实现
 * <p>
 * 实现数据备份和恢复功能，支持：
 * - 数据导出
 * - 备份验证
 * - 增量恢复
 * - 覆盖恢复
 * </p>
 *
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;
    private final EquipmentMapper equipmentMapper;
    private final FeedbackMapper feedbackMapper;
    private final ConsumeRecordMapper consumeRecordMapper;

    @Override
    public Map<String, Object> exportBackupData() {
        Map<String, Object> backupData = new HashMap<>();

        // 备份各表数据
        backupData.put("members", memberMapper.selectList(null));
        backupData.put("coaches", coachMapper.selectList(null));
        backupData.put("courses", courseMapper.selectList(null));
        backupData.put("equipments", equipmentMapper.selectList(null));
        backupData.put("feedbacks", feedbackMapper.selectList(null));
        backupData.put("consumeRecords", consumeRecordMapper.selectList(null));

        // 添加备份元信息
        backupData.put("backupTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        backupData.put("version", "1.0");

        return backupData;
    }

    @Override
    public Map<String, Object> getBackupStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("memberCount", memberMapper.selectCount(null));
        stats.put("coachCount", coachMapper.selectCount(null));
        stats.put("courseCount", courseMapper.selectCount(null));
        stats.put("equipmentCount", equipmentMapper.selectCount(null));
        stats.put("feedbackCount", feedbackMapper.selectCount(null));
        stats.put("consumeRecordCount", consumeRecordMapper.selectCount(null));

        return stats;
    }

    @Override
    public Map<String, Object> validateBackup(Map<String, Object> backupData) {
        Map<String, Object> result = new HashMap<>();
        int totalRecords = 0;

        // 验证各数据表
        if (backupData.containsKey("members")) {
            List<?> members = (List<?>) backupData.get("members");
            result.put("membersInBackup", members.size());
            totalRecords += members.size();
        }

        if (backupData.containsKey("coaches")) {
            List<?> coaches = (List<?>) backupData.get("coaches");
            result.put("coachesInBackup", coaches.size());
            totalRecords += coaches.size();
        }

        if (backupData.containsKey("courses")) {
            List<?> courses = (List<?>) backupData.get("courses");
            result.put("coursesInBackup", courses.size());
            totalRecords += courses.size();
        }

        if (backupData.containsKey("equipments")) {
            List<?> equipments = (List<?>) backupData.get("equipments");
            result.put("equipmentsInBackup", equipments.size());
            totalRecords += equipments.size();
        }

        if (backupData.containsKey("feedbacks")) {
            List<?> feedbacks = (List<?>) backupData.get("feedbacks");
            result.put("feedbacksInBackup", feedbacks.size());
            totalRecords += feedbacks.size();
        }

        if (backupData.containsKey("consumeRecords")) {
            List<?> consumeRecords = (List<?>) backupData.get("consumeRecords");
            result.put("consumeRecordsInBackup", consumeRecords.size());
            totalRecords += consumeRecords.size();
        }

        result.put("backupTime", backupData.get("backupTime"));
        result.put("version", backupData.get("version"));
        result.put("totalRecords", totalRecords);
        result.put("valid", true);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> restoreData(Map<String, Object> backupData, String mode) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> importedCounts = new HashMap<>();
        Map<String, Integer> skippedCounts = new HashMap<>();
        Map<String, List<String>> errors = new HashMap<>();

        boolean isOverwrite = "overwrite".equals(mode);

        log.info("开始数据恢复，模式：{}", isOverwrite ? "覆盖导入" : "增量导入");

        // 恢复会员数据
        if (backupData.containsKey("members")) {
            RestoreResult memberResult = restoreMembers(
                    (List<?>) backupData.get("members"), isOverwrite);
            importedCounts.put("members", memberResult.imported);
            skippedCounts.put("members", memberResult.skipped);
            if (!memberResult.errors.isEmpty()) {
                errors.put("members", memberResult.errors);
            }
        }

        // 恢复教练数据
        if (backupData.containsKey("coaches")) {
            RestoreResult coachResult = restoreCoaches(
                    (List<?>) backupData.get("coaches"), isOverwrite);
            importedCounts.put("coaches", coachResult.imported);
            skippedCounts.put("coaches", coachResult.skipped);
            if (!coachResult.errors.isEmpty()) {
                errors.put("coaches", coachResult.errors);
            }
        }

        // 恢复课程数据
        if (backupData.containsKey("courses")) {
            RestoreResult courseResult = restoreCourses(
                    (List<?>) backupData.get("courses"), isOverwrite);
            importedCounts.put("courses", courseResult.imported);
            skippedCounts.put("courses", courseResult.skipped);
            if (!courseResult.errors.isEmpty()) {
                errors.put("courses", courseResult.errors);
            }
        }

        // 恢复器材数据
        if (backupData.containsKey("equipments")) {
            RestoreResult equipmentResult = restoreEquipments(
                    (List<?>) backupData.get("equipments"), isOverwrite);
            importedCounts.put("equipments", equipmentResult.imported);
            skippedCounts.put("equipments", equipmentResult.skipped);
            if (!equipmentResult.errors.isEmpty()) {
                errors.put("equipments", equipmentResult.errors);
            }
        }

        // 恢复反馈数据
        if (backupData.containsKey("feedbacks")) {
            RestoreResult feedbackResult = restoreFeedbacks(
                    (List<?>) backupData.get("feedbacks"), isOverwrite);
            importedCounts.put("feedbacks", feedbackResult.imported);
            skippedCounts.put("feedbacks", feedbackResult.skipped);
            if (!feedbackResult.errors.isEmpty()) {
                errors.put("feedbacks", feedbackResult.errors);
            }
        }

        // 恢复消费记录数据
        if (backupData.containsKey("consumeRecords")) {
            RestoreResult consumeResult = restoreConsumeRecords(
                    (List<?>) backupData.get("consumeRecords"), isOverwrite);
            importedCounts.put("consumeRecords", consumeResult.imported);
            skippedCounts.put("consumeRecords", consumeResult.skipped);
            if (!consumeResult.errors.isEmpty()) {
                errors.put("consumeRecords", consumeResult.errors);
            }
        }

        // 计算总数
        int totalImported = importedCounts.values().stream().mapToInt(Integer::intValue).sum();
        int totalSkipped = skippedCounts.values().stream().mapToInt(Integer::intValue).sum();

        result.put("mode", isOverwrite ? "覆盖导入" : "增量导入");
        result.put("imported", importedCounts);
        result.put("skipped", skippedCounts);
        result.put("totalImported", totalImported);
        result.put("totalSkipped", totalSkipped);
        result.put("success", errors.isEmpty());

        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }

        String message = String.format("数据恢复完成，共导入 %d 条记录，跳过 %d 条记录",
                totalImported, totalSkipped);
        result.put("message", message);

        log.info("数据恢复完成：{}", result);

        return result;
    }

    /**
     * 恢复会员数据
     */
    private RestoreResult restoreMembers(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        if (isOverwrite) {
            // 覆盖模式：清空表（注意外键约束，这里简化处理）
            // 实际生产环境需要更谨慎处理
            log.warn("覆盖模式：不删除现有会员数据，仅更新已存在的记录");
        }

        for (Object item : data) {
            try {
                Member member = convertToEntity(item, Member.class);
                if (member.getId() == null) {
                    result.errors.add("会员记录缺少ID");
                    result.skipped++;
                    continue;
                }

                // 检查是否存在
                Member existing = memberMapper.selectById(member.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        // 覆盖模式：更新记录
                        member.setUpdateTime(LocalDateTime.now());
                        memberMapper.updateById(member);
                        result.imported++;
                    } else {
                        // 增量模式：跳过已存在的记录
                        result.skipped++;
                    }
                } else {
                    // 插入新记录
                    member.setCreateTime(LocalDateTime.now());
                    member.setUpdateTime(LocalDateTime.now());
                    memberMapper.insert(member);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("会员导入失败: " + e.getMessage());
                result.skipped++;
                log.error("会员数据恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 恢复教练数据
     */
    private RestoreResult restoreCoaches(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        for (Object item : data) {
            try {
                Coach coach = convertToEntity(item, Coach.class);
                if (coach.getId() == null) {
                    result.errors.add("教练记录缺少ID");
                    result.skipped++;
                    continue;
                }

                Coach existing = coachMapper.selectById(coach.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        coach.setUpdateTime(LocalDateTime.now());
                        coachMapper.updateById(coach);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } else {
                    coach.setCreateTime(LocalDateTime.now());
                    coach.setUpdateTime(LocalDateTime.now());
                    coachMapper.insert(coach);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("教练导入失败: " + e.getMessage());
                result.skipped++;
                log.error("教练数据恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 恢复课程数据
     */
    private RestoreResult restoreCourses(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        for (Object item : data) {
            try {
                Course course = convertToEntity(item, Course.class);
                if (course.getId() == null) {
                    result.errors.add("课程记录缺少ID");
                    result.skipped++;
                    continue;
                }

                Course existing = courseMapper.selectById(course.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        course.setUpdateTime(LocalDateTime.now());
                        courseMapper.updateById(course);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } else {
                    course.setCreateTime(LocalDateTime.now());
                    course.setUpdateTime(LocalDateTime.now());
                    courseMapper.insert(course);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("课程导入失败: " + e.getMessage());
                result.skipped++;
                log.error("课程数据恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 恢复器材数据
     */
    private RestoreResult restoreEquipments(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        for (Object item : data) {
            try {
                Equipment equipment = convertToEntity(item, Equipment.class);
                if (equipment.getId() == null) {
                    result.errors.add("器材记录缺少ID");
                    result.skipped++;
                    continue;
                }

                Equipment existing = equipmentMapper.selectById(equipment.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        equipment.setUpdateTime(LocalDateTime.now());
                        equipmentMapper.updateById(equipment);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } else {
                    equipment.setCreateTime(LocalDateTime.now());
                    equipment.setUpdateTime(LocalDateTime.now());
                    equipmentMapper.insert(equipment);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("器材导入失败: " + e.getMessage());
                result.skipped++;
                log.error("器材数据恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 恢复反馈数据
     */
    private RestoreResult restoreFeedbacks(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        for (Object item : data) {
            try {
                Feedback feedback = convertToEntity(item, Feedback.class);
                if (feedback.getId() == null) {
                    result.errors.add("反馈记录缺少ID");
                    result.skipped++;
                    continue;
                }

                Feedback existing = feedbackMapper.selectById(feedback.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        feedbackMapper.updateById(feedback);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } else {
                    feedback.setCreateTime(LocalDateTime.now());
                    feedbackMapper.insert(feedback);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("反馈导入失败: " + e.getMessage());
                result.skipped++;
                log.error("反馈数据恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 恢复消费记录数据
     */
    private RestoreResult restoreConsumeRecords(List<?> data, boolean isOverwrite) {
        RestoreResult result = new RestoreResult();

        for (Object item : data) {
            try {
                ConsumeRecord record = convertToEntity(item, ConsumeRecord.class);
                if (record.getId() == null) {
                    result.errors.add("消费记录缺少ID");
                    result.skipped++;
                    continue;
                }

                ConsumeRecord existing = consumeRecordMapper.selectById(record.getId());
                if (existing != null) {
                    if (isOverwrite) {
                        consumeRecordMapper.updateById(record);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } else {
                    record.setCreateTime(LocalDateTime.now());
                    consumeRecordMapper.insert(record);
                    result.imported++;
                }
            } catch (Exception e) {
                result.errors.add("消费记录导入失败: " + e.getMessage());
                result.skipped++;
                log.error("消费记录恢复失败", e);
            }
        }

        return result;
    }

    /**
     * 将Map/Object转换为实体类
     */
    @SuppressWarnings("unchecked")
    private <T> T convertToEntity(Object obj, Class<T> clazz) {
        if (obj instanceof Map) {
            return BeanUtil.toBean((Map<?, ?>) obj, clazz);
        } else if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        throw new IllegalArgumentException("无法转换数据类型: " + obj.getClass().getName());
    }

    /**
     * 恢复结果内部类
     */
    private static class RestoreResult {
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
    }
}
