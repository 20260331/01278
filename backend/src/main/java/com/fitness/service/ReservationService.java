package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.PageResult;
import com.fitness.entity.Reservation;

/**
 * 预约Service
 */
public interface ReservationService extends IService<Reservation> {

    /**
     * 分页查询预约记录
     */
    PageResult<Reservation> pageList(Page<Reservation> page, Long memberId, Long courseId, Integer status);

    /**
     * 创建预约
     */
    void createReservation(Long memberId, Long courseId);

    /**
     * 取消预约
     */
    void cancelReservation(Long id);

    /**
     * 课程签到
     */
    void checkin(Long id);
}
