package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.entity.Course;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 课程服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("课程服务测试")
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Test
    @DisplayName("测试分页查询课程")
    void testPageList() {
        Page<Course> page = new Page<>(1, 10);
        PageResult<Course> result = courseService.pageList(page, null, null, null, null);
        
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("测试按课程名称查询")
    void testPageListByName() {
        Page<Course> page = new Page<>(1, 10);
        PageResult<Course> result = courseService.pageList(page, "瑜伽", null, null, null);
        
        assertNotNull(result);
        result.getRecords().forEach(course -> 
            assertTrue(course.getName().contains("瑜伽")));
    }

    @Test
    @DisplayName("测试按课程类型查询")
    void testPageListByType() {
        Page<Course> page = new Page<>(1, 10);
        PageResult<Course> result = courseService.pageList(page, null, "瑜伽", null, null);
        
        assertNotNull(result);
        result.getRecords().forEach(course -> 
            assertEquals("瑜伽", course.getType()));
    }

    @Test
    @DisplayName("测试按教练ID查询")
    void testPageListByCoachId() {
        Page<Course> page = new Page<>(1, 10);
        PageResult<Course> result = courseService.pageList(page, null, null, 1L, null);
        
        assertNotNull(result);
        result.getRecords().forEach(course -> 
            assertEquals(1L, course.getCoachId()));
    }

    @Test
    @DisplayName("测试获取课程详情")
    void testGetById() {
        Course course = courseService.getById(1L);
        
        assertNotNull(course);
        assertEquals("晨间瑜伽", course.getName());
        assertEquals("瑜伽", course.getType());
    }

    @Test
    @DisplayName("测试获取可预约课程")
    void testGetAvailableCourses() {
        List<Course> courses = courseService.getAvailableCourses();
        
        assertNotNull(courses);
        // 验证返回的课程状态都是正常且未满
        courses.forEach(course -> {
            assertEquals(1, course.getStatus());
            assertTrue(course.getCurrentCount() < course.getMaxCapacity());
        });
    }

    @Test
    @DisplayName("测试创建课程")
    void testCreateCourse() {
        Course course = new Course();
        course.setName("测试课程");
        course.setType("健身");
        course.setCoachId(1L);
        course.setMaxCapacity(20);
        course.setPrice(new BigDecimal("99.00"));
        course.setDuration(60);
        course.setStartTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        course.setEndTime(LocalDateTime.of(2026, 3, 1, 11, 0));
        course.setLocation("健身房A");
        course.setDescription("测试课程描述");

        courseService.createCourse(course);

        assertNotNull(course.getId());
        assertEquals(0, course.getCurrentCount());
        assertEquals(1, course.getStatus());
    }

    @Test
    @DisplayName("测试更新课程")
    void testUpdateCourse() {
        Course course = courseService.getById(1L);
        course.setMaxCapacity(30);
        course.setPrice(new BigDecimal("88.00"));
        
        courseService.updateCourse(course);
        
        Course updated = courseService.getById(1L);
        assertEquals(30, updated.getMaxCapacity());
        assertEquals(new BigDecimal("88.00"), updated.getPrice());
    }

    @Test
    @DisplayName("测试删除课程")
    void testDeleteCourse() {
        // 先创建一个课程
        Course course = new Course();
        course.setName("待删除课程");
        course.setType("测试");
        course.setCoachId(1L);
        course.setMaxCapacity(10);
        course.setPrice(new BigDecimal("50.00"));
        course.setDuration(30);
        course.setStartTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        course.setEndTime(LocalDateTime.of(2026, 4, 1, 10, 30));
        course.setLocation("测试场地");
        
        courseService.createCourse(course);
        Long courseId = course.getId();
        
        // 删除课程
        courseService.removeById(courseId);
        
        // 验证删除成功
        Course deleted = courseService.getById(courseId);
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试获取不存在的课程")
    void testGetNonExistentCourse() {
        Course course = courseService.getById(9999L);
        assertNull(course);
    }

    @Test
    @DisplayName("测试课程价格验证")
    void testCoursePrice() {
        Course course = courseService.getById(1L);
        
        assertNotNull(course.getPrice());
        assertTrue(course.getPrice().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("测试课程容量验证")
    void testCourseCapacity() {
        Course course = courseService.getById(1L);
        
        assertTrue(course.getMaxCapacity() > 0);
        assertTrue(course.getCurrentCount() >= 0);
        assertTrue(course.getCurrentCount() <= course.getMaxCapacity());
    }
}
