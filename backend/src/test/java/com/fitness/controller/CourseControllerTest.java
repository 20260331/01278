package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.LoginDTO;
import com.fitness.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 课程管理接口测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("课程管理接口测试")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String memberToken;
    private String coachToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = getToken("admin", "123456");
        memberToken = getToken("member1", "123456");
        coachToken = getToken("coach1", "123456");
    }

    private String getToken(String username, String password) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"token\":\"") + 9;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    @Test
    @DisplayName("测试分页查询课程")
    void testPageCourses() throws Exception {
        mockMvc.perform(get("/api/v1/courses/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("测试按课程类型筛选")
    void testPageCoursesWithTypeFilter() throws Exception {
        mockMvc.perform(get("/api/v1/courses/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("type", "瑜伽"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取课程详情")
    void testGetCourseById() throws Exception {
        mockMvc.perform(get("/api/v1/courses/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("晨间瑜伽"));
    }

    @Test
    @DisplayName("测试获取可预约课程 - 会员权限")
    void testGetAvailableCoursesAsMember() throws Exception {
        mockMvc.perform(get("/api/v1/courses/available")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试获取可预约课程 - 非会员无权限")
    void testGetAvailableCoursesAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/courses/available")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试新增课程 - 管理员权限")
    void testCreateCourse() throws Exception {
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

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试新增课程 - 非管理员无权限")
    void testCreateCourseAsNonAdmin() throws Exception {
        Course course = new Course();
        course.setName("测试课程2");
        course.setType("健身");

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + coachToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试修改课程")
    void testUpdateCourse() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setName("晨间瑜伽-更新");
        course.setType("瑜伽");
        course.setCoachId(1L);
        course.setMaxCapacity(30);
        course.setPrice(new BigDecimal("78.00"));

        mockMvc.perform(put("/api/v1/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除课程")
    void testDeleteCourse() throws Exception {
        // 先创建一个课程用于删除
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

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk());

        // 获取最新创建的课程ID并删除
        mockMvc.perform(delete("/api/v1/courses/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取请假申请列表 - 管理员")
    void testGetLeaveListAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/courses/leave/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取请假申请列表 - 教练")
    void testGetLeaveListAsCoach() throws Exception {
        mockMvc.perform(get("/api/v1/courses/leave/page")
                        .header("Authorization", "Bearer " + coachToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试未授权访问")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/courses/page"))
                .andExpect(status().isUnauthorized());
    }
}
