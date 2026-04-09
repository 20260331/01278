package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Venue;

/**
 * 场地Service
 */
public interface VenueService extends IService<Venue> {

    /**
     * 分页查询场地
     */
    PageResult<Venue> pageList(Page<Venue> page, String name, String type, Integer status);
}
