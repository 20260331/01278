package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Equipment;

/**
 * 器材Service
 */
public interface EquipmentService extends IService<Equipment> {

    /**
     * 分页查询器材
     */
    PageResult<Equipment> pageList(Page<Equipment> page, String name, String type, Integer status);

    /**
     * 记录维护
     */
    void maintain(Long id, String remark);
}
