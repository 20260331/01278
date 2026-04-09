package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Equipment;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.EquipmentMapper;
import com.fitness.service.EquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 器材Service实现
 */
@Slf4j
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public PageResult<Equipment> pageList(Page<Equipment> page, String name, String type, Integer status) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Equipment::getName, name);
        wrapper.eq(StringUtils.hasText(type), Equipment::getType, type);
        wrapper.eq(status != null, Equipment::getStatus, status);
        wrapper.orderByDesc(Equipment::getCreateTime);
        
        return PageResult.of(page(page, wrapper));
    }

    @Override
    public void maintain(Long id, String remark) {
        Equipment equipment = getById(id);
        if (equipment == null) {
            throw new BusinessException("器材不存在");
        }
        
        equipment.setLastMaintainDate(LocalDate.now());
        equipment.setNextMaintainDate(LocalDate.now().plusMonths(3));
        equipment.setRemark(remark);
        equipment.setStatus(1);
        updateById(equipment);
        
        log.info("器材维护: {}", equipment.getName());
    }
}
