package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.PasswordDTO;
import com.fitness.service.SysUserService;
import com.fitness.vo.LoginVO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证接口测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证接口测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserService sysUserService;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // 获取管理员token用于后续测试
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // 从响应中提取token
        if (response.contains("token")) {
            int start = response.indexOf("\"token\":\"") + 9;
            int end = response.indexOf("\"", start);
            adminToken = response.substring(start, end);
        }
    }

    @Test
    @DisplayName("测试用户登录 - 成功")
    void testLoginSuccess() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("测试用户登录 - 密码错误")
    void testLoginWithWrongPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("测试用户登录 - 用户名为空")
    void testLoginWithEmptyUsername() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试获取当前用户信息 - 成功")
    void testGetCurrentUserSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/info")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("测试获取当前用户信息 - 未授权")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("测试退出登录")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试教练登录")
    void testCoachLogin() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("coach1");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("ROLE_COACH"));
    }

    @Test
    @DisplayName("测试会员登录")
    void testMemberLogin() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("member1");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("ROLE_MEMBER"));
    }
}
