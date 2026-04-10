package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.WaitingList;

public interface WaitingListService extends IService<WaitingList> {

    PageResult<WaitingList> pageList(Page<WaitingList> page, Long memberId, Long courseId, Integer status);

    void joinWaitingList(Long memberId, Long courseId);

    void cancelWaitingList(Long id);

    WaitingList processNextWaitingMember(Long courseId);
}
