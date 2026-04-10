package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.PageResult;
import com.fitness.entity.Course;
import com.fitness.entity.WaitingList;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.WaitingListMapper;
import com.fitness.service.CourseService;
import com.fitness.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingListServiceImpl extends ServiceImpl<WaitingListMapper, WaitingList> implements WaitingListService {

    private final CourseService courseService;

    @Override
    public PageResult<WaitingList> pageList(Page<WaitingList> page, Long memberId, Long courseId, Integer status) {
        return PageResult.of(baseMapper.selectPageWithDetail(page, memberId, courseId, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinWaitingList(Long memberId, Long courseId) {
        if (count(new LambdaQueryWrapper<WaitingList>()
                .eq(WaitingList::getMemberId, memberId)
                .eq(WaitingList::getCourseId, courseId)
                .eq(WaitingList::getStatus, 0)) > 0) {
            throw new BusinessException("您已在该课程候补队列中");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (course.getStatus() != 1 && course.getStatus() != 2) {
            throw new BusinessException("课程不可候补");
        }
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法候补");
        }

        Integer queueOrder = baseMapper.getNextQueueOrder(courseId);

        WaitingList waitingList = new WaitingList();
        waitingList.setMemberId(memberId);
        waitingList.setCourseId(courseId);
        waitingList.setStatus(0);
        waitingList.setQueueOrder(queueOrder);
        save(waitingList);

        log.info("加入候补队列: memberId={}, courseId={}, queueOrder={}", memberId, courseId, queueOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitingList(Long id) {
        WaitingList waitingList = getById(id);
        if (waitingList == null) {
            throw new BusinessException("候补记录不存在");
        }
        if (waitingList.getStatus() != 0) {
            throw new BusinessException("该候补记录无法取消");
        }

        waitingList.setStatus(2);
        updateById(waitingList);

        log.info("取消候补: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaitingList processNextWaitingMember(Long courseId) {
        List<WaitingList> waitingMembers = baseMapper.selectByCourseIdOrderByQueueOrder(courseId);
        if (waitingMembers.isEmpty()) {
            return null;
        }

        WaitingList next = waitingMembers.get(0);
        next.setStatus(1);
        updateById(next);

        log.info("候补成员补位: waitingListId={}, memberId={}, courseId={}",
                next.getId(), next.getMemberId(), courseId);
        return next;
    }
}
