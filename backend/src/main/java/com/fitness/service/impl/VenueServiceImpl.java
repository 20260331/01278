package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Venue;
import com.fitness.mapper.VenueMapper;
import com.fitness.service.VenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 场地Service实现
 */
@Slf4j
@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Override
    public PageResult<Venue> pageList(Page<Venue> page, String name, String type, Integer status) {
        LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Venue::getName, name);
        wrapper.eq(StringUtils.hasText(type), Venue::getType, type);
        wrapper.eq(status != null, Venue::getStatus, status);
        wrapper.orderByDesc(Venue::getCreateTime);
        
        return PageResult.of(page(page, wrapper));
    }
}
